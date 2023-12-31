package com.barmetler.workload

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter

@TestConfiguration
@EntityScan("com.barmetler.workload.models")
@ComponentScan(
    "com.barmetler.workload",
    excludeFilters = [Filter(classes = [ApplicationMain::class])]
)
class TestConfig
