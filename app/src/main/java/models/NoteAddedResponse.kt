package com.example.securenotes.models

data class NoteAddedResponse(
    val success: Boolean,
    val message: String,
    val insertId: Int
)
