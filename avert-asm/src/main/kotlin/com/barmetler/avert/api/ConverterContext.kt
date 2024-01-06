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

package com.barmetler.avert.api

import kotlin.reflect.KClass

interface ConverterContext {
    /** get or create the converter for this specific pair of types. */
    fun <Domain : Any, Proto : Any> getConverter(
        domainClass: KClass<Domain>,
        protoClass: KClass<Proto>
    ): Converter<Domain, Proto>

    fun <Domain : Any, Proto : Any> toProto(
        domainClass: KClass<Domain>,
        protoClass: KClass<Proto>,
        domain: Domain,
    ) = getConverter(domainClass, protoClass).toProto(domain, this)

    fun <Domain : Any, Proto : Any> toDomain(
        domainClass: KClass<Domain>,
        protoClass: KClass<Proto>,
        proto: Proto,
    ) = getConverter(domainClass, protoClass).toDomain(proto, this)
}

inline fun <reified Domain : Any, reified Proto : Any> ConverterContext.getConverter() =
    getConverter(Domain::class, Proto::class)

inline fun <reified Domain : Any, reified Proto : Any> ConverterContext.toProto(domain: Domain) =
    toProto(Domain::class, Proto::class, domain)

inline fun <reified Domain : Any, reified Proto : Any> ConverterContext.toDomain(proto: Proto) =
    toDomain(Domain::class, Proto::class, proto)
