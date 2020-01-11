package com.example.user

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = uuid("id").primaryKey()
    val username = text("username")
    val password = text("password")
}