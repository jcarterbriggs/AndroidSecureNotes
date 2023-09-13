package com.example.securenotes.models

import android.widget.ImageButton


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.securenotes.R


interface NoteItemListener {
    fun onDeleteClick(note: Note)
    fun onNoteClick(note: Note)
}
class NotesAdapter(private val notes: List<Note>, private val listener: NoteItemListener): RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitle)
        val body: TextView = itemView.findViewById(R.id.noteBody)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(notes[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.body.text = note.body
        holder.itemView.setOnClickListener {
            listener.onNoteClick(note)
        }
    }


    override fun getItemCount() = notes.size
}
