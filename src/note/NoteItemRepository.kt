package com.example.noteitem

import org.jetbrains.exposed.sql.Table

object NoteItems : Table() {
    val id = uuid("id").primaryKey()
    val title = text("title")
    val details = text("details")
    val assignedTo = text("assignedto")
    val importance =  text("importance")
}