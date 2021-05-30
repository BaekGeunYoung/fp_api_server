package com.example.fp_api_server.service

import arrow.Kind
import com.example.fp_api_server.ConcurrentMappable
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.exception.EntityNotFoundException
import com.example.fp_api_server.repository.UserRepository

interface UserService<F> {
    fun findById(id: Long): Kind<F, User>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
    fun findAllAndUpdate(): Kind<F, Unit>
}

class UserServiceImpl<F> (
    private val M: Monad<F>,
    private val CM: ConcurrentMappable<F>,
    private val userRepository: UserRepository<F>,
) : UserService<F>, Monad<F> by M {
    override fun findById(id: Long): Kind<F, User> =
        userRepository.findById(id).map {
            if (it.isPresent) it.get()
            else throw EntityNotFoundException(User::class, id)
        }

    override fun findAll(): Kind<F, List<User>> =
        userRepository.findAll()

    override fun delete(id: Long): Kind<F, Unit> =
        findById(id).flatMap {
            userRepository.delete(id)
        }

    override fun update(user: User): Kind<F, Unit> =
        findById(user.id).flatMap {
            userRepository.update(user)
        }

    override fun insert(user: User): Kind<F, Unit> =
        userRepository.insert(user)

    override fun findAllAndUpdate(): Kind<F, Unit> = CM.run {
        findAll()
            .concurrentMap {
                val updated = it.copy(name = "fake name")
                userRepository.update(updated)
            }
            .map { Unit }
    }
}
