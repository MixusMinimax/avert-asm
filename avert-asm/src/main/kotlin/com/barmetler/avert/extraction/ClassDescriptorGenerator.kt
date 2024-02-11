/*
 * Copyright (c) 2023-2024 Maximilian Barmetler <http://barmetler.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.barmetler.avert.extraction

import arrow.core.Either
import arrow.core.partially1
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.recover
import arrow.optics.copy
import com.barmetler.avert.annotation.ProtoClass
import com.barmetler.avert.annotation.ProtoField
import com.barmetler.avert.api.Converter
import com.barmetler.avert.dto.ClassDescriptor
import com.barmetler.avert.dto.FieldDescriptor
import com.barmetler.avert.dto.protoFieldDescriptor
import com.barmetler.avert.dto.toDomainDescriptor
import com.barmetler.avert.dto.toDomainFieldAnnotation
import com.barmetler.avert.errors.ExtractionError
import com.barmetler.avert.errors.ExtractionError.InvalidProtoClass
import com.barmetler.avert.strategy.ProtoFieldNameComparingStrategy
import com.barmetler.avert.util.asMutable
import com.barmetler.avert.util.asSubclassOf
import com.barmetler.avert.util.findAnnotationOrRaise
import com.barmetler.avert.util.firstOrRaise
import com.barmetler.avert.util.getCanonicalConstructor
import com.barmetler.avert.util.isEmptyCallable
import com.barmetler.avert.util.javaFieldName
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.full.valueParameters

interface ClassDescriptorGenerator {
    fun classDescriptorOf(
        domainClass: KClass<*>,
    ): Either<ExtractionError, ClassDescriptor>
}

class ClassDescriptorGeneratorImpl
@Inject
constructor(protoFieldNameComparingStrategy: ProtoFieldNameComparingStrategy) :
    ClassDescriptorGenerator, ProtoFieldNameComparingStrategy by protoFieldNameComparingStrategy {
    private val classDescriptors = ConcurrentHashMap<KClass<*>, ClassDescriptor>()

    override fun classDescriptorOf(
        domainClass: KClass<*>,
    ): Either<ExtractionError, ClassDescriptor> = either {
        classDescriptors.computeIfAbsent(domainClass) { generateClassDescriptor(it).bind() }
    }

    private fun generateClassDescriptor(
        domainClass: KClass<*>,
    ): Either<ExtractionError, ClassDescriptor> = either {
        val annotation = domainClass.findAnnotationOrRaise<ProtoClass>()

        val protoClass = annotation.protoClass

        // TODO: check converter generics
        val customConverter = annotation.getCustomConverter()

        if (customConverter != null) {
            return@either ClassDescriptor(
                domainClass = domainClass,
                protoClass = protoClass,
                customConverter = customConverter
            )
        }

        val protoMessage = protoClass.asSubclassOf<Message>() ?: raise(InvalidProtoClass)
        val protoDescriptor = protoMessage.getProtoDescriptor()

        val canonicalConstructor = domainClass.getCanonicalConstructor()

        logger.warn { "ProtoClass descriptor for $protoClass:\n$protoDescriptor" }

        val fields = run {
            val javaAccessors =
                domainClass.memberFunctions
                    .asSequence()
                    .map { it.javaFieldName() }
                    .groupingBy { it.name }
                    .fold(null as FieldDescriptor?) { accNull, field ->
                        (accNull ?: FieldDescriptor(field.name)).let { acc ->
                            when {
                                field.isSetter -> acc.copy(setter = field.function)
                                else -> acc.copy(getter = field.function)
                            }
                        }
                    }

            val propertyNames = domainClass.memberProperties.asSequence().map { it.name }.toSet()

            val syntheticJavaAccessors =
                javaAccessors
                    .asSequence()
                    .filter { (key, _) -> key !in propertyNames }
                    .mapNotNull { (_, v) -> v }
                    .map {
                        it.copy(
                            protoFieldDescriptor =
                                FieldDescriptor.ProtoFieldDescriptors(
                                    toProtoFieldAnnotation = it.getter?.protoFieldAnnotation,
                                    toDomainFieldAnnotation = it.setter?.protoFieldAnnotation,
                                )
                        )
                    }

            (domainClass.memberProperties.asSequence().map { property ->
                    val javaProperty = javaAccessors[property.name]
                    val toProtoFieldAnnotation =
                        javaProperty?.getter?.protoFieldAnnotation
                            ?: property.getter.protoFieldAnnotation
                            ?: property.protoFieldAnnotation
                    val toDomainFieldAnnotation =
                        javaProperty?.setter?.protoFieldAnnotation
                            ?: property.asMutable?.setter?.protoFieldAnnotation
                            ?: property.protoFieldAnnotation
                    FieldDescriptor(
                        name = property.name,
                        field = property,
                        getter = javaProperty?.getter ?: property.getter,
                        setter = javaProperty?.setter ?: property.asMutable?.setter,
                        protoFieldDescriptor =
                            FieldDescriptor.ProtoFieldDescriptors(
                                toProtoFieldAnnotation = toProtoFieldAnnotation,
                                toDomainFieldAnnotation = toDomainFieldAnnotation,
                            ),
                    )
                } + syntheticJavaAccessors)
                .filter {
                    it.protoFieldDescriptor.toProtoFieldAnnotation != null ||
                        it.protoFieldDescriptor.toDomainFieldAnnotation != null
                }
                .map { fieldDescriptor -> fieldDescriptor }
                .associateByTo(mutableMapOf()) { it.name }
        }

        val domainConstructor = canonicalConstructor ?: domainClass.findDomainConstructor(fields)

        domainConstructor?.valueParameters?.forEach { parameter ->
            val name = parameter.name ?: return@forEach
            fields[name] =
                fields
                    .computeIfAbsent(name) { _ -> FieldDescriptor(name = name) }
                    .copy(constructorArgument = parameter)
                    .let {
                        parameter.protoFieldAnnotation?.let(
                            FieldDescriptor.protoFieldDescriptor.toDomainFieldAnnotation::set
                                .partially1(it)
                        ) ?: it
                    }
        }

        fields.replaceAll { domainName, fieldDescriptor ->
            fieldDescriptor.copy {
                if (fieldDescriptor.protoFieldDescriptor.toDomainFieldAnnotation != null) {
                    recover(
                        {
                            FieldDescriptor.protoFieldDescriptor.toDomainDescriptor set
                                with(protoDescriptor) {
                                    fieldDescriptor.protoFieldDescriptor.toDomainFieldAnnotation
                                        .getProtoFieldDescriptor(domainName)
                                }
                        },
                        { logger.warn { it } }
                    )
                }
                if (fieldDescriptor.protoFieldDescriptor.toProtoFieldAnnotation != null) {
                    recover(
                        {
                            FieldDescriptor.protoFieldDescriptor.toDomainDescriptor set
                                with(protoDescriptor) {
                                    fieldDescriptor.protoFieldDescriptor.toProtoFieldAnnotation
                                        .getProtoFieldDescriptor(domainName)
                                }
                        },
                        { logger.warn { it } }
                    )
                }
            }
        }

        logger.warn {
            "Generating class descriptor for ${domainClass.simpleName} with ${fields.size} fields\n" +
                "$fields"
        }

        ClassDescriptor(
            domainClass = domainClass,
            protoClass = protoClass,
            domainConstructor = domainConstructor,
            fields = fields.values.toList(),
        )
    }

    context(Raise<InvalidProtoClass>)
    private fun KClass<out Message>.getProtoDescriptor() =
        staticFunctions
            .firstOrRaise({ InvalidProtoClass }) { function ->
                function.name == "getDescriptor" && function.isEmptyCallable
            }
            .run { call() as? Descriptors.Descriptor ?: raise(InvalidProtoClass) }

    context(Raise<ExtractionError.NotImplemented>)
    private fun ProtoClass.getCustomConverter() =
        converter
            .takeUnless { it.isAbstract }
            ?.let { customConverter ->
                val converterType =
                    customConverter.allSupertypes.firstOrRaise({ ExtractionError.NotImplemented }) {
                        it.classifier == Converter::class
                    }
                converterType.let {}
                customConverter
            }

    context(Raise<ExtractionError.ProtoFieldNotFound>, Descriptors.Descriptor)
    private fun ProtoField.getProtoFieldDescriptor(
        domainName: String
    ): Descriptors.FieldDescriptor {
        val protoFieldName = name.ifEmpty { domainName }
        return fields.find { protoFieldNameEquals(protoFieldName, it.name) }
            ?: raise(ExtractionError.ProtoFieldNotFound(protoFieldName))
    }

    /**
     * Search for constructor that only has arguments that either:
     * - annotated by ProtoField
     * - already present in the list of fields
     * - have a default value ordered by number of arguments descending. This is necessary for a
     *   common pattern:
     * - a java class with AllArgsConstructor or RequiredArgsConstructor.
     */
    private fun KClass<*>.findDomainConstructor(fields: Map<String, Any?>?) =
        constructors
            .filter { constructor ->
                constructor.valueParameters.all { parameter ->
                    when {
                        parameter.isOptional -> true
                        fields != null && parameter.name in fields.keys -> true
                        parameter.hasAnnotation<ProtoField>() -> true
                        else -> false
                    }
                }
            }
            .maxByOrNull { it.valueParameters.size }

    private val KAnnotatedElement.protoFieldAnnotation: ProtoField?
        get() = annotations.asSequence().filterIsInstance<ProtoField>().firstOrNull()

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
