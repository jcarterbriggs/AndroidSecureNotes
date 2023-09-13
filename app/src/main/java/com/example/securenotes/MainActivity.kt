package com.example.securenotes
import com.example.securenotes.models.NoteItemListener


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
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


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
        notesAdapter = NotesAdapter(listOf(), object : NoteItemListener {
            override fun onDeleteClick(note: Note) {
                confirmDeleteNote(note)
            }
            override fun onNoteClick(note: Note) {   // Ensure you have this method
                showEditNoteDialog(note)
            }
        })

        notesRecyclerView.adapter = notesAdapter
        val addNoteButton: FloatingActionButton = findViewById(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            showAddNoteDialog()
        }

        fetchNotes()  // Fetch notes when app starts
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
                    notesAdapter = NotesAdapter(notesList, object : NoteItemListener {
                        override fun onDeleteClick(note: Note) {
                            confirmDeleteNote(note)
                        }
                        override fun onNoteClick(note: Note) {
                            showEditNoteDialog(note)
                        }
                    })


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
    fun showEditNoteDialog(note: Note) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50)

        val titleInput = EditText(this)
        titleInput.hint = "Title"
        titleInput.setText(note.title)
        layout.addView(titleInput)

        val bodyInput = EditText(this)
        bodyInput.hint = "Body"
        bodyInput.setText(note.body)
        layout.addView(bodyInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                val newBody = bodyInput.text.toString().trim()

                if (newTitle.isNotEmpty() && newBody.isNotEmpty()) {
                    editNoteInApi(note.id, newTitle, newBody)
                } else {
                    Toast.makeText(this, "Title and Body cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    private fun confirmDeleteNote(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes") { _, _ ->
                deleteNoteFromApi(note.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteNoteFromApi(noteId: Int) {
        val call = notesApi.deleteNote(noteId)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchNotes()
                } else {
                    Toast.makeText(this@MainActivity, "Error deleting note", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editNoteInApi(noteId: Int, title: String, body: String) {
        val editedNote = Note(id = noteId, title = title, body = body)

        lifecycleScope.launch {
            val response = notesApi.editNote(noteId, editedNote)
            if (response.isSuccessful) {
                // handle successful response here
                val updatedNote = response.body()
                Toast.makeText(this@MainActivity, "Note edited successfully", Toast.LENGTH_SHORT).show()
                fetchNotes()
            } else {
                Toast.makeText(this@MainActivity, "Error editing note", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
