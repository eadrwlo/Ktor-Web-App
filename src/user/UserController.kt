package com.example.user

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.ArrayList

class UserController {
    fun getAll(): ArrayList<UserItem> {
        val users: ArrayList<UserItem> = arrayListOf()

        transaction {
            Users.selectAll().map {
                users.add(
                    UserItem(
                        id = it[Users.id],
                        username = it[Users.username],
                        password = it[Users.password]
                    )
                )
            }
        }
        return users
    }

    fun insert(user: UserDTO) {
        transaction {
            Users.insert {
                it[id] = UUID.randomUUID()
                it[username] = user.username
                it[password] = user.password
            }
        }
    }

//    fun update(user: UserDTO, id: UUID) {
//        transaction {
//            Users.update({Users.id eq id}) {
//                it[age] = user.age
//                it[firstname] = user.firstName
//                it[lastname] = user.lastName
//            }
//        }
//    }

    fun delete(id: UUID) {
        transaction {
            Users.deleteWhere { Users.id eq id }
        }
    }
}