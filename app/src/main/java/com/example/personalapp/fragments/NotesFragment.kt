package com.example.personalapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.personalapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotesFragment : Fragment() {

    private lateinit var adapter: ArrayAdapter<String> // Create an adapter
    private val notesList = ArrayList<String>() // Create a list for note items
    private lateinit var database: DatabaseReference // Declare the DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false) // Inflate the layout for this fragment

        // Initialize the FirebaseDatabase instance and DatabaseReference
        database = FirebaseDatabase.getInstance().reference

        val lvNotes = view.findViewById<ListView>(R.id.lvNotes) // Assign note field to a variable
        val btnAddNote = view.findViewById<Button>(R.id.btnAddNote) // Assign Add button to a variable
        val btnClearNotes = view.findViewById<Button>(R.id.btnClearNotes) // Assign Clear notes to a variable

        // Sets up the ArrayAdapter with a simple list item layout and the notes list
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, notesList)
        lvNotes.adapter = adapter

        loadNotesFromFirebase() // Add this line to load notes

        btnAddNote.setOnClickListener {// Sets a click listener to Add note button
            addNote() // Performs the addNote function when clicked
        }

        btnClearNotes.setOnClickListener { // Sets a click listener to Clear notes button
            notesList.clear() // If the button is clicked, clears the notes
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "All notes cleared", Toast.LENGTH_SHORT).show()

            // Deletes all notes from Firebase Realtime Database
            deleteAllNotesFromFirebase()
        }

        return view
        
    }

    // Function for a adding a note
    private fun addNote() {
        val editText = EditText(requireContext())
        AlertDialog.Builder(requireContext())// Use AlertDialog to add a note
            .setTitle("Add Note")
            .setView(editText)
            .setPositiveButton("Add") { dialog, which ->
                val note = editText.text.toString()
                if (note.isNotEmpty()) {
                    notesList.add(note)
                    adapter.notifyDataSetChanged()

                    // Insert the note into the Firebase Realtime Database
                    val noteId = database.push().key // Generate a unique ID for the note
                    if (noteId != null) {
                        database.child("notes").child(noteId).setValue(note)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun deleteAllNotesFromFirebase()
    {
        val notesReference = database.child("notes")
        notesReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "All notes deleted from Firebase", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete notes from Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNotesFromFirebase() {
        val notesReference = database.child("notes")
        notesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notesList.clear() // Clear existing data to avoid duplicates

                for (noteSnapshot in dataSnapshot.children) {
                    val note = noteSnapshot.getValue(String::class.java)
                    if (note != null) {
                        notesList.add(note)
                    }
                }

                adapter.notifyDataSetChanged() // Notify the adapter to refresh the list view
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log error or show an error message to the user
                Toast.makeText(requireContext(), "Failed to load notes.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
