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

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

internal val <T> KProperty<T>.asMutable: KMutableProperty<T>?
    get() = this as? KMutableProperty<T>

internal inline fun <reified T : Any> KClass<*>.asSubclassOf(): KClass<T>? =
    if (this.isSubclassOf(T::class)) {
        @Suppress("UNCHECKED_CAST")
        this as KClass<T>
    } else {
        null
    }
