package com.example.fp_api_server.repository

import arrow.Kind
import com.example.fp_api_server.entity.User
import java.util.Optional

interface UserRepository<F> {
    fun findById(id: Long): Kind<F, Optional<User>>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
}
