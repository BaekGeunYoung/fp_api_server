package com.example.fp_api_server.repository.impl

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.IOOf
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.repository.UserRepository
import java.util.Optional

class IOUserRepository(
    private val userJpaRepository: UserJpaRepository
): UserRepository<ForIO> {
    override fun findById(id: Long): IOOf<Optional<User>> = IO {
        userJpaRepository.findById(id)
    }

    override fun findAll(): IOOf<List<User>> = IO {
        userJpaRepository.findAll()
    }

    override fun delete(id: Long): IOOf<Unit> = IO {
        userJpaRepository.deleteById(id)
    }

    override fun update(user: User): IOOf<Unit> = IO {
        userJpaRepository.save(user)
    }

    override fun insert(user: User): IOOf<Unit> = IO {
        userJpaRepository.save(user)
    }
}
