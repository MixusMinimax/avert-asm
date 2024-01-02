package com.barmetler.workload

import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.some
import com.barmetler.workload.dto.HumanNameChangesetDto
import com.barmetler.workload.dto.PersonalDetailsChangesetDto
import com.barmetler.workload.dto.UserChangesetDto
import com.barmetler.workload.models.HumanName
import com.barmetler.workload.models.PersonalDetails
import com.barmetler.workload.models.User
import com.barmetler.workload.services.UserService
import java.time.LocalDateTime
import java.time.ZoneId
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

    @Test
    fun updateUser() = assertRight {
        val email = "updateUser@example.com"
        val user = sut.createUser(email).bind()
        sut.updateUser(
                user.id,
                UserChangesetDto(
                    personalDetails =
                        PersonalDetailsChangesetDto(
                            name =
                                HumanNameChangesetDto(
                                    firstName = "Maxi".some(),
                                    lastName = "Barmetler".some(),
                                    middleNames = listOf("Erich")
                                ),
                        ),
                ),
            )
            .bind()
        val updatedUser = sut.getUser(user.id).bind()
        val expectedUser =
            User().apply {
                id = user.id
                this.email = email
                personalDetails =
                    PersonalDetails(
                        name =
                            HumanName(
                                firstName = "Maxi",
                                lastName = "Barmetler",
                                middleNames = listOf("Erich"),
                            )
                    )
            }
        assertEquals(expectedUser, updatedUser)

        val dateOfBirth =
            LocalDateTime.of(2000, 4, 2, 15, 10).run {
                atOffset(ZoneId.of("Europe/Berlin").rules.getOffset(this))
            }
        sut.updateUser(
                user.id,
                UserChangesetDto(
                    personalDetails =
                        PersonalDetailsChangesetDto(
                            name = HumanNameChangesetDto(middleNames = emptyList()),
                            dateOfBirth = dateOfBirth.some(),
                        ),
                ),
            )
            .bind()
        val expectedUser2 =
            User().apply {
                id = user.id
                this.email = email
                personalDetails =
                    PersonalDetails(
                        name =
                            HumanName(
                                firstName = "Maxi",
                                lastName = "Barmetler",
                                middleNames = emptyList(),
                            ),
                        dateOfBirth = dateOfBirth,
                    )
            }
        assertEquals(expectedUser2, sut.getUser(user.id).bind())
    }
}

inline fun <Error> assertRight(block: Raise<Error>.() -> Unit) {
    val result = either(block)
    assert(result.isRight()) { result.leftOrNull() ?: Unit }
}
