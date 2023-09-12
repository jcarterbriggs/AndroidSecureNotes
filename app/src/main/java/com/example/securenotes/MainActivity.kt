package com.example.securenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.securenotes.models.Note
import com.example.securenotes.models.NotesAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.securenotes.api.NotesApi
import com.example.securenotes.api.ServiceGenerator
import android.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.example.securenotes.models.NoteAddedResponse


class MainActivity : AppCompatActivity() {
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesRecyclerView: RecyclerView
    private val notesApi by lazy { ServiceGenerator.createService(NotesApi::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize notesRecyclerView
        notesRecyclerView = findViewById(R.id.notesRecyclerView)

        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(listOf())  // empty list for now
        notesRecyclerView.adapter = notesAdapter
        val addNoteButton: FloatingActionButton = findViewById(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            addNoteButton.setOnClickListener {
                showAddNoteDialog()
            }
        }
    }
    private fun showAddNoteDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50)

        val titleInput = EditText(this)
        titleInput.hint = "Title"
        layout.addView(titleInput)

        val bodyInput = EditText(this)
        bodyInput.hint = "Body"
        layout.addView(bodyInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Note")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val body = bodyInput.text.toString().trim()

                if (title.isNotEmpty() && body.isNotEmpty()) {
                    addNoteToApi(title, body)
                } else {
                    Toast.makeText(this, "Title and Body cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    private fun addNoteToApi(title: String, body: String) {
        val newNote = Note(id = 0, title = title, body = body) // id=0 because it's auto-incremented in the DB
        val call = notesApi.addNote(newNote)
        call.enqueue(object : Callback<NoteAddedResponse> {
            override fun onResponse(call: Call<NoteAddedResponse>, response: Response<NoteAddedResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Note added successfully", Toast.LENGTH_SHORT).show()
                    fetchNotes()
                } else {
                    Toast.makeText(this@MainActivity, "Error adding note", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NoteAddedResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchNotes() {
        val call = notesApi.getAllNotes()
        call.enqueue(object : Callback<List<Note>> {
            override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                if (response.isSuccessful) {
                    val notesList = response.body() ?: listOf()
                    notesAdapter = NotesAdapter(notesList)
                    notesRecyclerView.adapter = notesAdapter
                } else {
                    Toast.makeText(this@MainActivity, "Error fetching notes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Note>>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

}
