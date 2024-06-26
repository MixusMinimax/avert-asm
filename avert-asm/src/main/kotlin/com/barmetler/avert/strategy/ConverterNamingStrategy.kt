/*
 * Copyright (c) 2024 Maximilian Barmetler <http://barmetler.com>
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

package com.barmetler.avert.strategy

import kotlin.reflect.KClass

interface ConverterNamingStrategy {
    /**
     * Returns the name of the converter for the given domain class.
     *
     * @param domainClass the domain class to get the converter name for.
     * @return the name of the converter for the given domain class.
     */
    fun getConverterName(domainClass: KClass<*>): String?
}
