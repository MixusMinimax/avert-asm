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

import com.barmetler.avert.api.Converter
import com.barmetler.avert.dto.ClassDescriptor
import com.barmetler.avert.strategy.ConverterNamingStrategy
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureVisitor
import org.objectweb.asm.signature.SignatureWriter

class AsmCompiler
@Inject
constructor(private val converterNamingStrategy: ConverterNamingStrategy) : Compiler {

    override fun generateConverter(descriptor: ClassDescriptor) {
        val domainClass = descriptor.domainClass
        val protoClass = descriptor.protoClass
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        // extends object, implements Converter<domainClass, protoClass>
        val sw = SignatureWriter()
        scoped(sw) {
            scoped(visitSuperclass()) { visitClassType(Type.getInternalName(Any::class.java)) }
            scoped(visitInterface()) {
                visitClassType(Type.getInternalName(Converter::class.java))
                scoped(visitTypeArgument('=')) {
                    visitClassType(Type.getInternalName(domainClass.java))
                }
                scoped(visitTypeArgument('=')) {
                    visitClassType(Type.getInternalName(protoClass.java))
                }
            }
        }
        logger.info { "Signature: $sw" }
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER,
            converterNamingStrategy.getConverterName(domainClass),
            sw.toString(),
            Type.getInternalName(Any::class.java),
            null
        )
    }

    private inline fun scoped(sv: SignatureVisitor, block: SignatureVisitor.() -> Unit) {
        sv.block()
        sv.visitEnd()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
