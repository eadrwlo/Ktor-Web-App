package com.example.noteitem

import java.util.*

data class NoteItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val details: String,
    val assignedTo: String,
    val importance: String
)

data class NoteItemDTO(
    val title: String,
    val details: String,
    val assignedTo: String,
    val importance: String
)

enum class Importance {
    LOW, MEDIUM, HIGH
}
