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

package com.barmetler.avert.compile

import com.barmetler.avert.dto.ClassDescriptor
import com.barmetler.avert.strategy.ConverterNamingStrategy
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject
import org.objectweb.asm.ClassWriter

class AsmCompiler
@Inject
constructor(private val converterNamingStrategy: ConverterNamingStrategy) : Compiler {

    fun generateConverter(descriptor: ClassDescriptor) {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        classWriter.newClass(converterNamingStrategy.getConverterName(descriptor.domainClass))
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
