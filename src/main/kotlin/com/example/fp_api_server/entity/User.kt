package com.example.fp_api_server.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class User(
    @Id
    @GeneratedValue
    val id: Long = 0,

    @Column
    val name: String,

    @Column
    val age: Int
)
