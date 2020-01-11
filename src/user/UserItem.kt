package com.example.user


import java.util.*

data class UserItem(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String
)

data class UserDTO(
    val username: String,
    val password: String
)
