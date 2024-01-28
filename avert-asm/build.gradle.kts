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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
}

repositories { mavenCentral() }

dependencies {
    // Reflect
    implementation(kotlin("reflect"))

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.1")

    // Arrow
    constraints { ksp("io.arrow-kt:arrow-optics-ksp-plugin:1.2.1") }
    implementation(platform("io.arrow-kt:arrow-stack:1.2.1"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("io.arrow-kt:arrow-optics")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin")
    implementation(kotlin("stdlib-jdk8"))

    // Dagger
    implementation("com.google.dagger:dagger:2.48")
    ksp("com.google.dagger:dagger-compiler:2.48")

    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")

    // Asm
    implementation("org.ow2.asm:asm:9.6")
}

kotlin { jvmToolchain(17) }

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions { freeCompilerArgs += "-Xcontext-receivers" }
}
