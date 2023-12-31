package com.barmetler.workload

import arrow.core.raise.Raise
import arrow.core.raise.either
import com.barmetler.workload.services.UserService
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(private val sut: UserService) {
    @Test fun contextLoads() {}

    @Test
    fun createUser() {
        val email = "user@example.com"
        val result = sut.createUser(email)
        assert(result.isRight { it.email == email })
    }

    @Test
    fun getUser() = assertRight {
        val email = "getUser@example.com"
        val createResult = sut.createUser(email).bind()
        val getResult = sut.getUser(createResult.id).bind()
        assertEquals(createResult.id, getResult.id)
        assertEquals(email, createResult.email)
        assertEquals(email, getResult.email)
    }
}

fun <Error> assertRight(block: Raise<Error>.() -> Unit) {
    assert(either(block).isRight())
}
