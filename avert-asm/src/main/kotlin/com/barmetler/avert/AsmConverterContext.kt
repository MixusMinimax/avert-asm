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

package com.barmetler.avert

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import com.barmetler.avert.api.Converter
import com.barmetler.avert.api.ConverterContext
import com.barmetler.avert.compile.CompileModule
import com.barmetler.avert.compile.Compiler
import com.barmetler.avert.extraction.ClassDescriptorGenerator
import com.barmetler.avert.extraction.ExtractionModule
import com.barmetler.avert.strategy.StrategyModule
import dagger.Component
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Singleton
import kotlin.reflect.KClass

class AsmConverterContext : ConverterContext, AutoCloseable {

    @Singleton
    @Component(modules = [StrategyModule::class, ExtractionModule::class, CompileModule::class])
    internal interface Implementation {
        fun classDescriptorGenerator(): ClassDescriptorGenerator

        fun compiler(): Compiler

        @Component.Builder
        interface Builder {
            fun build(): Implementation
        }
    }

    private val implementation = DaggerAsmConverterContext_Implementation.create()
    private val classDescriptorGenerator = implementation.classDescriptorGenerator()
    private val compiler = implementation.compiler()
    private val converterCache =
        ConcurrentHashMap<Pair<KClass<*>, KClass<*>>, Future<out Converter<*, *>>>()
    private val executor = Executors.newCachedThreadPool()

    override fun <Domain : Any, Proto : Any> getConverter(
        domainClass: KClass<Domain>,
        protoClass: KClass<Proto>
    ): Converter<Domain, Proto> {
        val key = domainClass to protoClass
        @Suppress("UNCHECKED_CAST")
        return converterCache
            .getOrPut(key) {
                executor.submit<Converter<Domain, Proto>> {
                    createConverter(domainClass, protoClass).getOrElse {
                        logger.error { "Failed to create converter for $key" }
                        throw IllegalStateException(it.toString())
                    }
                }
            }
            .get() as Converter<Domain, Proto>
    }

    private fun <Domain : Any, Proto : Any> createConverter(
        domainClass: KClass<Domain>,
        protoClass: KClass<Proto>,
    ): Either<Any, Converter<Domain, Proto>> = either {
        val classDescriptor = classDescriptorGenerator.classDescriptorOf(domainClass).bind()
        compiler.generateConverter(classDescriptor)
        object : Converter<Domain, Proto> {
            override fun toProto(domain: Domain?, context: ConverterContext): Proto? {
                TODO()
            }

            override fun toDomain(proto: Proto?, context: ConverterContext): Domain? {
                TODO()
            }
        }
    }

    override fun close() {
        executor.shutdown()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
