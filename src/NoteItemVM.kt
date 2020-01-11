package com.example

import com.example.noteitem.NoteItem

data class NoteItemVM(private val notes: List<NoteItem>, private val username: String) {
    val userName = username
    val noteItems = notes
}
