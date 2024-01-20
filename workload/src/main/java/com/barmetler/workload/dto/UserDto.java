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

package com.barmetler.workload.dto;

import com.barmetler.avert.annotation.ProtoClass;
import com.barmetler.avert.annotation.ProtoField;
import com.barmetler.proto.person.PersonalDetails;

import java.util.UUID;

@ProtoClass(protoClass = com.barmetler.proto.user.User.class)
public record UserDto(
        @ProtoField UUID id,
        @ProtoField String email,
        @ProtoField String passwordHash,
        @ProtoField PersonalDetails personalDetails
) {
}
