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

package com.barmetler.workload.mappers;

import com.barmetler.avert.api.Converter;
import com.barmetler.avert.api.ConverterContext;
import com.barmetler.proto.core.DateTime;
import com.barmetler.workload.models.HumanName;
import com.barmetler.workload.models.PersonalDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class PersonalDetailsConverter implements Converter<PersonalDetails, com.barmetler.proto.person.PersonalDetails> {

    private @Nullable Converter<HumanName, com.barmetler.proto.person.HumanName> humanNameConverter;
    private @Nullable Converter<OffsetDateTime, DateTime> dateTimeConverter;

    @Override
    public final @Nullable com.barmetler.proto.person.PersonalDetails toProto(
            final @Nullable PersonalDetails personalDetails,
            final @NotNull ConverterContext context
    ) {
        if (personalDetails == null) {
            return null;
        }

        if (humanNameConverter == null) {
            humanNameConverter = context.getConverter(HumanName.class, com.barmetler.proto.person.HumanName.class);
        }

        if (dateTimeConverter == null) {
            dateTimeConverter = context.getConverter(OffsetDateTime.class, DateTime.class);
        }

        final com.barmetler.proto.person.PersonalDetails.Builder builder = com.barmetler.proto.person.PersonalDetails.newBuilder();

        final com.barmetler.proto.person.HumanName humanNameMsg = humanNameConverter.toProto(personalDetails.getName(), context);
        if (humanNameMsg != null) {
            builder.setName(humanNameMsg);
        }

        final DateTime dateOfBirthMsg = dateTimeConverter.toProto(personalDetails.getDateOfBirth(), context);
        if (dateOfBirthMsg != null) {
            builder.setDateOfBirth(dateOfBirthMsg);
        }

        return builder.build();
    }


    @Override
    public final @Nullable PersonalDetails toDomain(
            final @Nullable com.barmetler.proto.person.PersonalDetails personalDetails,
            final @NotNull ConverterContext context
    ) {
        return null;
    }
}
