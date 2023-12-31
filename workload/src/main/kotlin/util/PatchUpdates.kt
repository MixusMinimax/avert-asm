package util

import arrow.core.Either
import arrow.core.Option
import arrow.core.plus
import arrow.core.raise.either
import com.barmetler.workload.errors.ApplicationError
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

inline fun <reified I : Any, reified C : Any> patchInstance(
    instance: I,
    changeset: C,
): Either<ApplicationError, Unit> = patchInstance(instance, I::class, changeset, C::class)

@Suppress("UNCHECKED_CAST")
fun <I : Any, C : Any> patchInstance(
    instance: I,
    instanceType: KClass<I>,
    changeset: C,
    changesetType: KClass<C>,
): Either<ApplicationError, Unit> = either {
    val instanceFields = instanceType.memberProperties.associateBy { it.name }
    for ((field, value, instanceField) in
        changesetType.memberProperties
            .asSequence()
            .map { it to it.get(changeset) }
            .mapNotNull { it.bothOrNull() }
            .map { p -> p + instanceFields[p.first.name] }
            .mapNotNull { (a, b, c) ->
                if (c is KMutableProperty1<I, *>) {
                    Triple(a, b, c)
                } else {
                    null
                }
            }) {
        instanceField.isAccessible = true
        when (val fieldWrapperKind = field.getFieldWrapperKind(instanceField)) {
            FieldWrapperKind.OPTION,
            FieldWrapperKind.VALUE -> {
                val v =
                    if (fieldWrapperKind == FieldWrapperKind.OPTION) {
                        val valueGetter = field.getValueGetter()
                        valueGetter?.invoke(value)
                    } else {
                        value
                    }
                instanceField.trySet(instance, v)
            }
            FieldWrapperKind.COLLECTION -> {
                val instanceValue = instanceField.get(instance)
                (instanceValue as? MutableCollection<*> to value as? Collection<*>)
                    .takeIf { (a, b) -> a != null && b != null }
                    ?.let { (target, source) ->
                        target!!.clear()
                        (target as MutableCollection<Any?>).addAll((source as Collection<Any?>))
                    }
            }
            FieldWrapperKind.CHANGESET -> {
                val instanceValue = instanceField.get(instance)
                val i = instanceField.returnType.classifier
                val c = field.returnType.classifier
                if (instanceValue != null && i is KClass<*> && c is KClass<*>) {
                    patchInstance(instanceValue, i as KClass<Any>, value, c as KClass<Any>)
                }
            }
        }
    }
}

private fun <T, V> KProperty1<T, V>.getFieldWrapperKind(
    targetProperty: KProperty1<*, *>,
): FieldWrapperKind {
    val type = returnType.classifier
    val targetType = targetProperty.returnType.classifier
    return when {
        type == Option::class ||
            type == Optional::class &&
                (targetType != Option::class && targetType != Optional::class) ->
            FieldWrapperKind.OPTION
        type is KClass<*> &&
            type.isSubclassOf(Collection::class) &&
            targetType is KClass<*> &&
            targetType.isSubclassOf(MutableCollection::class) -> FieldWrapperKind.COLLECTION
        type is KClass<*> && targetType is KClass<*> && type.isSubclassOf(targetType) ->
            FieldWrapperKind.VALUE
        else -> FieldWrapperKind.CHANGESET
    }
}

private enum class FieldWrapperKind {
    OPTION,
    COLLECTION,
    CHANGESET,
    VALUE,
}

private fun <T, V> KProperty1<T, V>.getValueGetter() =
    when (returnType.classifier) {
        Option::class -> { a: Any? -> (a as? Option<*>)?.getOrNull() }
        Optional::class -> { a: Any? -> (a as? Optional<*>)?.getOrNull() }
        else -> null
    }

private fun <T, V : Any> KMutableProperty1<T, *>.trySet(instance: T, value: V?) {
    val castSelf =
        if (
            value == null ||
                setter.valueParameters[0].type.classifier.let {
                    it is KClass<*> && value::class.isSubclassOf(it)
                }
        ) {
            @Suppress("UNCHECKED_CAST")
            this as KMutableProperty1<T, V?>
        } else {
            return
        }
    castSelf.isAccessible = true
    castSelf.set(instance, value)
}
