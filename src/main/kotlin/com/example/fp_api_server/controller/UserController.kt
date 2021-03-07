package com.example.fp_api_server.controller

import com.example.fp_api_server.entity.User
import com.example.fp_api_server.repository.UserRepository
import com.example.fp_api_server.typeclass.Suspendable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

open class UserController<F>(
    private val suspendable: Suspendable<F>,
    private val userRepository: UserRepository<F>
) {
    @GetMapping("")
    suspend fun listUsers(): List<User> = suspendable.run {
        userRepository.findAll().suspended()
    }

    @PostMapping("")
    suspend fun addUser(@RequestBody user: User): Unit = suspendable.run {
        userRepository.insert(user).suspended()
    }
}
