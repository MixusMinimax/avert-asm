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

package com.barmetler.workload.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.recover
import com.barmetler.workload.dto.UserChangesetDto
import com.barmetler.workload.errors.ApplicationError
import com.barmetler.workload.errors.CrudError
import com.barmetler.workload.models.User
import com.barmetler.workload.repositories.UserRepository
import com.barmetler.workload.util.patchInstance
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {
    @Transactional
    fun createUser(email: String): Either<CrudError.AlreadyExistsError<String>, User> = either {
        userRepository.findByEmail(email)?.let {
            logger.warn { "User with email $email already exists" }
            raise(CrudError.AlreadyExistsError(email))
        }
        User().apply { this.email = email }.save()
    }

    @Transactional(readOnly = true)
    fun getUser(id: UUID): Either<CrudError.NotFoundError<UUID>, User> = either {
        userRepository.findById(id).orElseGet { raise(CrudError.NotFoundError(id)) }
    }

    @Transactional
    fun updateUser(
        id: UUID,
        changeset: UserChangesetDto,
    ): Either<CrudError, User> = either {
        val user = getUser(id).bind()
        recover(
            { patchInstance(user, changeset).bind() },
            { _: ApplicationError -> raise(CrudError.UpdateFailed("patch failed")) },
            { exc -> raise(CrudError.UpdateFailed(exc.message ?: "$exc")) }
        )
        user
    }

    @Transactional
    fun deleteUser(id: UUID): Either<CrudError, Unit> = either {
        val user = getUser(id).bind()
        userRepository.delete(user)
    }

    private fun User.save() = userRepository.save(this)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
