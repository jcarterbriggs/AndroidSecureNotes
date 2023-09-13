package com.example.securenotes.api

import com.example.securenotes.models.Note
import com.example.securenotes.models.NoteAddedResponse
import com.example.securenotes.models.NotesAdapter
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotesApi {

    @GET("notes")
    fun getAllNotes(): Call<List<Note>>

    @PUT("notes/{id}")
    suspend fun editNote(@Path("id") id: Int, @Body note: Note): Response<Note>

    @POST("notes")
    fun addNote(@Body note: Note): Call<NoteAddedResponse>

    @DELETE("notes/{id}")
    fun deleteNote(@Path("id") noteId: Int): Call<Void>
}
