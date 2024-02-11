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

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class DefaultStrategy @Inject constructor() :
    ConverterNamingStrategy, ProtoFieldNameComparingStrategy {
    override fun getConverterName(domainClass: KClass<*>): String? {
        return domainClass.qualifiedName?.let { qualifiedName ->
            val match = DOMAIN_CLASS_REGEX.matchEntire(qualifiedName)
            match?.let {
                val packageName = match.groupValues[1]
                val className = match.groupValues[2]
                val converterName = "$packageName${className}_Converter"
                converterName
            }
        }
    }

    override fun protoFieldNameEquals(
        suppliedProtoFieldName: String,
        actualProtoFieldName: String
    ): Boolean =
        suppliedProtoFieldName.uniqueProtoFieldName() == actualProtoFieldName.uniqueProtoFieldName()

    private fun String.uniqueProtoFieldName(): String {
        return lowercase(Locale.getDefault()).replace(PROTO_FIELD_IGNORED_CHARS, "")
    }

    companion object {
        private val DOMAIN_CLASS_REGEX = Regex("^((?:[^.]+\\.)*)([^.]*)$")
        private val PROTO_FIELD_IGNORED_CHARS = Regex("_")
    }
}
