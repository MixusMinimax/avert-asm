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

package com.barmetler.workload.initializer

import com.barmetler.proto.person.HumanName as HumanNameMsg
import com.barmetler.proto.user.User as UserMsg
import com.barmetler.avert.api.AsmConverterContext
import com.barmetler.avert.api.ConverterContext
import com.barmetler.avert.api.getConverter
import com.barmetler.workload.models.HumanName
import com.barmetler.workload.models.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class Example {
    @EventListener(ApplicationReadyEvent::class)
    fun example() {
        val converterContext: ConverterContext = AsmConverterContext()
        converterContext.getConverter<User, UserMsg>()
        converterContext.getConverter<HumanName, HumanNameMsg>()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}