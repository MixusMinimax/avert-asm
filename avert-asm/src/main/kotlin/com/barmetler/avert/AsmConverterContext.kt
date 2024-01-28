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

import com.barmetler.avert.annotation.ProtoClass
import com.barmetler.avert.api.Converter
import com.barmetler.avert.api.ConverterContext
import com.barmetler.avert.compile.CompileModule
import com.barmetler.avert.compile.Compiler
import com.barmetler.avert.extraction.ClassDescriptorGenerator
import com.barmetler.avert.extraction.ExtractionModule
import com.barmetler.avert.strategy.StrategyModule
import dagger.Component
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

class AsmConverterContext : ConverterContext {

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

    override fun <Domain : Any, Proto : Any> getConverter(
        domainClass: KClass<Domain>,
        protoClass: KClass<Proto>
    ): Converter<Domain, Proto> {
        val annotation = domainClass.findAnnotations(ProtoClass::class).firstOrNull()
        logger.info { annotation }

        classDescriptorGenerator.classDescriptorOf(domainClass)

        return object : Converter<Domain, Proto> {
            override fun toProto(domain: Domain, context: ConverterContext?): Proto {
                TODO()
            }

            override fun toDomain(proto: Proto, context: ConverterContext?): Domain {
                TODO()
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
