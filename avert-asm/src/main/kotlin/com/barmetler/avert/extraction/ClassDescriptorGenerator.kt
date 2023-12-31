/*
 * Copyright (c) 2023 Maximilian Barmetler <http://barmetler.com>
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
import arrow.core.raise.either
import com.barmetler.avert.annotation.ProtoClass
import com.barmetler.avert.dto.ClassDescriptor
import com.barmetler.avert.errors.ExtractionError
import com.barmetler.avert.util.firstOrRaise
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

interface ClassDescriptorGenerator {
    fun classDescriptorOf(
        domainClass: KClass<*>,
    ): Either<ExtractionError, ClassDescriptor>
}

class ClassDescriptorGeneratorImpl
@Inject
constructor(
    val fieldDescriptorGenerator: FieldDescriptorGenerator,
) : ClassDescriptorGenerator {
    private val classDescriptors = mutableMapOf<KClass<*>, ClassDescriptor>()

    override fun classDescriptorOf(
        domainClass: KClass<*>,
    ): Either<ExtractionError, ClassDescriptor> = either {
        classDescriptors.computeIfAbsent(domainClass) { generateClassDescriptor(it).bind() }
    }

    private fun generateClassDescriptor(
        domainClass: KClass<*>,
    ): Either<ExtractionError, ClassDescriptor> = either {
        val annotation =
            domainClass.annotations.filterIsInstance<ProtoClass>().firstOrRaise {
                ExtractionError.AnnotationMissing
            }

        val fields = domainClass.memberProperties

        logger.debug {
            "Generating class descriptor for ${domainClass.simpleName} with ${fields.size} fields"
        }

        TODO()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}