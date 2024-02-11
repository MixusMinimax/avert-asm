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

package com.barmetler.avert.dto

import arrow.optics.optics
import com.barmetler.avert.annotation.ProtoField
import com.google.protobuf.Descriptors
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

@optics
data class FieldDescriptor(
    val name: String,
    val field: KProperty<*>? = null,
    val getter: KFunction<*>? = null,
    val setter: KFunction<*>? = null,
    val constructorArgument: KParameter? = null,
    val protoFieldDescriptor: ProtoFieldDescriptors = ProtoFieldDescriptors(),
) {
    @optics
    data class ProtoFieldDescriptors(
        val toProtoFieldAnnotation: ProtoField? = null,
        val toProtoDescriptor: Descriptors.FieldDescriptor? = null,
        val toProtoFieldType: KClass<*>? = null,
        val toDomainFieldAnnotation: ProtoField? = null,
        val toDomainDescriptor: Descriptors.FieldDescriptor? = null,
        val toDomainFieldType: KClass<*>? = null,
    ) {
        companion object
    }

    companion object
}
