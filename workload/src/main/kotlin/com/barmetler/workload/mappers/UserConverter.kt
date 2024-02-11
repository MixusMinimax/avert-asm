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

package com.barmetler.workload.mappers

import com.barmetler.proto.person.PersonalDetails as PersonalDetailsMsg
import com.barmetler.proto.user.User as UserMsg
import com.barmetler.avert.api.Converter
import com.barmetler.avert.api.ConverterContext
import com.barmetler.workload.models.PersonalDetails
import com.barmetler.workload.models.User
import java.util.*

class UserConverter : Converter<User, UserMsg> {

    @Volatile private var uuidConverter: Converter<UUID, String>? = null
    @Volatile
    private var personalDetailsConverter: Converter<PersonalDetails, PersonalDetailsMsg>? = null

    override fun toProto(domain: User?, context: ConverterContext): UserMsg? {
        if (domain == null) {
            return null
        }

        // it is fine that this operation is not atomic, as the getConverter function is.
        // This might make things a bit slower at startup, but as soon as humanNameConverter is set,
        // future calls will be faster.
        if (uuidConverter == null) {
            uuidConverter = context.getConverter(UUID::class, String::class)
        }
        if (personalDetailsConverter == null) {
            personalDetailsConverter =
                context.getConverter(PersonalDetails::class, PersonalDetailsMsg::class)
        }

        val resultBuilder = UserMsg.newBuilder()

        val domainId = domain.id
        if (domainId != null) {
            resultBuilder.id = uuidConverter?.toProto(domainId, context) ?: ""
        }

        val domainEmail = domain.email
        if (domainEmail != null) {
            resultBuilder.email = domainEmail
        }

        val domainPasswordHash = domain.passwordHash
        if (domainPasswordHash != null) {
            resultBuilder.passwordHash = domainPasswordHash
        }

        val domainPersonalDetails = domain.personalDetails
        if (domainPersonalDetails != null) {
            val personalDetailsMsg =
                personalDetailsConverter?.toProto(domainPersonalDetails, context)
            if (personalDetailsMsg != null) {
                resultBuilder.personalDetails = personalDetailsMsg
            }
        }

        return resultBuilder.build()
    }

    override fun toDomain(proto: UserMsg?, context: ConverterContext): User? {
        TODO("Not yet implemented")
    }
}
