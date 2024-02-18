package com.example.personalapp.fragments

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.example.personalapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TodoFragment : Fragment() {

    private lateinit var todoAdapter: ArrayAdapter<String> // Facilitates the dynamic display of a collection of strings in a list format
    private val todoList = ArrayList<TodoItem>()
    private lateinit var database: DatabaseReference // Declare the DatabaseReference

    // Declare the valueEventListener at the class level
    // Acts as a bridge between the app and Firebase database, monitoring for changes like additions, deletions and updates
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the FirebaseDatabase instance and DatabaseReference
        database = FirebaseDatabase.getInstance().reference

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_todo, container, false)

        val etTodo = view.findViewById<EditText>(R.id.etTodo) // Assign EditText variable
        val btnAddTodo = view.findViewById<Button>(R.id.btnAddTodo) // Assign Button variable
        val lvTodos = view.findViewById<ListView>(R.id.lvTodos) // Assign list to a variable

        todoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList<String>())
        lvTodos.adapter = todoAdapter


        // Sets a click listener for button btnAddTodo
        btnAddTodo.setOnClickListener {
            val newTodoText = etTodo.text.toString() // Retrieves the text currently entered in the EditText field
            if (newTodoText.isNotEmpty()) { // Checks if user has entered any text
                val todoId = database.push().key // Generate a unique ID for the item
                if (todoId != null) {
                    val newTodoItem = TodoItem(todoId, newTodoText)
                    database.child("todo").child(todoId).setValue(newTodoText).addOnCompleteListener { // Saves the item to database
                        if (it.isSuccessful) {
                            // Rely on loadTodos() for UI updates after Firebase write
                            loadTodos()
                            etTodo.text.clear() // Clear the EditText
                        }
                    }
                }
            }
        }

        loadTodos() // Load the todos from Firebases

        // Sets a long click listener on the items in the todoList
        lvTodos.setOnItemLongClickListener { _, _, position, _ ->
            val todoItem = todoList[position]
            // Builds and displays an alert dialog asking the users for confirmation before deleting items
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, which ->
                    database.child("todo").child(todoItem.id).removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Run UI updates on the main thread to ensure thread safety
                            activity?.runOnUiThread {
                                // It's safer to re-fetch the todos from Firebase after deletion
                                // or ensure the local list is correctly updated before notifying the adapter
                                loadTodos() // Refetches the list from Firebase database to ensure the UI reflects the deletion
                                Toast.makeText(requireContext(), "Todo deleted", Toast.LENGTH_SHORT).show()
                            }
                        } else { // Shows a failure message
                            Toast.makeText(requireContext(), "Failed to delete todo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("No", null) // Closes the dialog without deleting the item
                .show()
            true // Indicate that the long click was handled
        }

        return view
    }


    private fun loadTodos() {
        // Checks if a valueEventListener is already attached to the todo node in the database
        // If it exists, it is removed using removeEventListener, preventing creation of duplicate listeners
        if (valueEventListener != null) {
            database.child("todo").removeEventListener(valueEventListener!!)
        }

        // Creates a new valueEventListener object that has two methods, onDataChange() and onCancelled()
        valueEventListener = object : ValueEventListener {
            // Whenever a change in the database occurs, the onDataChange() method of valueEventListener is triggered
            // The dataSnapShot represents the current state of the node. Its children hold individual items.
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                todoList.clear()                                                // Ensures existing data doesn't lead to duplicates
                dataSnapshot.children.mapNotNullTo(todoList) { snapshot ->      // Loops through child objects of the snapshot
                    val text = snapshot.getValue(String::class.java)
                    val id = snapshot.key
                    if (text != null && id != null) TodoItem(id, text) else null // A new item is created and added to the todoList
                }
                // Refreshes the UI with the latest data in the todoList
                updateTodoAdapter()
            }

            // If there's an error reading data, this method shows a toast message to the user
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load todos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Attach the listener to the database node
        database.child("todo").addValueEventListener(valueEventListener!!)
    }

    // Method to encapsulate the update logic, making the code more organized and reusable
    private fun updateTodoAdapter() {
        val todoTexts = todoList.map { it.text }
        todoAdapter.clear()
        todoAdapter.addAll(todoTexts)
        todoAdapter.notifyDataSetChanged()
    }



    data class TodoItem(val id: String, val text: String)

}
