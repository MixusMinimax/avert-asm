/*
 * Copyright (c) 2023 Maximilian Barmetler <http://barmetler.com>
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

import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    kotlin("plugin.lombok") version "1.9.22"
    id("com.google.protobuf") version "0.9.4"
    id("io.freefair.lombok") version "8.1.0"
}

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    implementation(project(":avert-asm"))

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.1")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.1" }
    generateProtoTasks { all().forEach { it.builtins { id("kotlin") } } }
}
