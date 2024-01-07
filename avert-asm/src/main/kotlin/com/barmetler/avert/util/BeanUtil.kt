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

package com.barmetler.avert.util

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

private fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
}

private fun String.decapitalize() = replaceFirstChar { it.lowercase(Locale.getDefault()) }

/**
 * Returns the name of the JVM getter method for this property.
 *
 * Only to be used with java beans.
 */
internal val KProperty<*>.javaGetterName: String
    get() =
        when {
            // internal boolean, must not be java Boolean wrapper
            (returnType.classifier as? KClass<*>)?.java == Boolean::class.java ->
                "is${name.capitalize()}"
            else -> "get${name.capitalize()}"
        }

/**
 * Returns the name of the JVM setter method for this property.
 *
 * Only to be used with java beans.
 */
internal val KMutableProperty<*>.javaSetterName: String
    get() = "set${name.capitalize()}"

internal data class JavaFieldResult(
    val name: String,
    val function: KFunction<*>,
    val isSetter: Boolean
)

internal fun KFunction<*>.javaFieldName(): JavaFieldResult? {
    val name = name
    val isSetter = name.startsWith("set")
    val isGetter = name.startsWith("get") || name.startsWith("is")
    if (!isSetter && !isGetter) return null
    val fieldName = name.substring(3)
    return JavaFieldResult(fieldName.decapitalize(), this, isSetter)
}
