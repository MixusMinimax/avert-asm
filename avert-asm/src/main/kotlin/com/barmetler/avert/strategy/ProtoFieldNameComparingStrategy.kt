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

package com.barmetler.avert.strategy

interface ProtoFieldNameComparingStrategy {
    /**
     * Compares the supplied proto field name with the actual proto field name.
     *
     * If protobuf files follow
     * [Google's protobuf style guide](https://protobuf.dev/programming-guides/style/#message-field-names),
     * their names will be in `snake_case`.
     *
     * The domain side, however, usually follows `camelCase`.
     *
     * The default implementation of this function adheres to Protobuf's uniqueness rule: Field
     * names must be unique after being converted to all lower case, with underscores removed. This
     * means, this conversion can also be used for equality.
     *
     * This function is used to find matching proto fields by using the domain field name, or, if
     * supplied, [com.barmetler.avert.annotation.ProtoField.name].
     *
     * @param suppliedProtoFieldName The name of the proto field as supplied by the user, or the
     *   domain field name.
     * @param actualProtoFieldName The actual name of the proto field.
     * @return `true` if the supplied proto field name matches the actual proto field name.
     */
    fun protoFieldNameEquals(suppliedProtoFieldName: String, actualProtoFieldName: String): Boolean
}
