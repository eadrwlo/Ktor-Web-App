package com.example.noteitem

import com.example.user.UserDTO
import com.example.user.UserItem
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.ArrayList

class NoteItemController {
    fun getAll(user: String): ArrayList<NoteItem> {
        val noteItems: ArrayList<NoteItem> = arrayListOf()

        transaction {
            NoteItems.selectAll().map {
                if (it[NoteItems.assignedTo] == user)
                {
                    noteItems.add(
                        NoteItem(
                            id = it[NoteItems.id],
                            title = it[NoteItems.title],
                            details = it[NoteItems.details],
                            assignedTo = it[NoteItems.assignedTo],
                            importance = it[NoteItems.importance]
                        )
                    )
                }

            }
        }
        return noteItems
    }

    fun insert(note: NoteItemDTO) {
        transaction {
            NoteItems.insert {
                it[id] = UUID.randomUUID()
                it[title] = note.title
                it[details] = note.details
                it[assignedTo] = note.assignedTo
                it[importance] = note.importance
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
            NoteItems.deleteWhere { NoteItems.id eq id }
        }
    }
}