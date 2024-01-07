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

import com.google.protobuf.Descriptors
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

data class FieldDescriptor(
    val name: String,
    val field: KProperty<*>? = null,
    val getter: KFunction<*>? = null,
    val setter: KFunction<*>? = null,
    val protoFieldDescriptor: ProtoFieldDescriptors? = null,
) {
    data class ProtoFieldDescriptors(
        val fieldDescriptor: Descriptors.FieldDescriptor? = null,
        val toProtoDescriptor: Descriptors.MethodDescriptor? = null,
        val toDomainDescriptor: Descriptors.MethodDescriptor? = null,
    )
}
