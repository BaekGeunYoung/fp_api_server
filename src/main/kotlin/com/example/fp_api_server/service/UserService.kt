package com.example.fp_api_server.service

import arrow.Kind
import arrow.fx.fix
import arrow.core.Either
import arrow.core.extensions.ListKTraverse
import arrow.core.k
import arrow.fx.typeclasses.Async
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.exception.EntityNotFoundException
import com.example.fp_api_server.repository.UserRepository

interface UserService<F> {
    fun findById(id: Long): Kind<F, User>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
}

class UserServiceImpl<F> (
    private val A: Async<F>,
    private val userRepository: UserRepository<F>,
) : UserService<F>, Async<F> by A {
    override fun findById(id: Long): Kind<F, User> =
        userRepository.findById(id).map {
            if (it.isPresent) it.get()
            else throw EntityNotFoundException(User::class, id)
        }

    override fun findAll(): Kind<F, List<User>> =
        userRepository.findAll()

    override fun delete(id: Long): Kind<F, Unit> =
        userRepository.findById(id).flatMap {
            userRepository.delete(id)
        }

    override fun update(user: User): Kind<F, Unit> =
        userRepository.findById(user.id).flatMap {
            userRepository.update(user)
        }

    override fun insert(user: User): Kind<F, Unit> =
        userRepository.insert(user)
}
