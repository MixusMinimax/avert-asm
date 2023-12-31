package com.barmetler.workload

import com.barmetler.workload.services.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(private val userService: UserService) {
    @Test fun contextLoads() {}

    @Test
    fun createUser() {
        val result = userService.createUser("user@example.com")
        assert(result.isRight())
    }
}
