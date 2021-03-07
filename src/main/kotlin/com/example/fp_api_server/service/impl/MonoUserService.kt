package com.example.fp_api_server.service.impl

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoKOf
import arrow.fx.reactor.extensions.monok.monad.flatMap
import arrow.fx.reactor.extensions.monok.monad.map
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.exception.EntityNotFoundException
import com.example.fp_api_server.repository.UserRepository
import com.example.fp_api_server.service.UserService

class MonoUserService(
    private val userRepository: UserRepository<ForMonoK>
): UserService<ForMonoK> {
    override fun findById(id: Long): MonoKOf<User> =
        userRepository.findById(id).map { 
            if (it.isPresent) it.get()
            else throw EntityNotFoundException(User::class, id)
        }

    override fun findAll(): MonoKOf<List<User>> =
        userRepository.findAll()

    override fun delete(id: Long): MonoKOf<Unit> =
        findById(id).flatMap {
            userRepository.delete(id)
        }

    override fun update(user: User): MonoKOf<Unit> =
        findById(user.id).flatMap {
            userRepository.update(user)
        }

    override fun insert(user: User): MonoKOf<Unit> =
        userRepository.update(user)
}