package com.example.personalapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.personalapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var greetingTextView: TextView
    private lateinit var dateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize the TextViews with the correct view reference
        greetingTextView = view.findViewById(R.id.tvGreeting) // Make sure IDs match your XML
        dateTextView = view.findViewById(R.id.tvDate)


        // Set greeting and date
        setGreetingAndDate() // Pass the view if needed, but it's not used in your current method so this is optional

        // Return the view at the end
        return view
    }

    private fun setGreetingAndDate() {
        // Set the greeting based on the current hour
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning,"
            in 12..17 -> "Good afternoon,"
            else -> "Good evening,"
        }
        greetingTextView.text = greeting

        // Format the current date
        val today = Date()

        // Get the day of the week, e.g., "Wednesday"
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(today)

        // Get the month and day, e.g., "January 1"
        val monthDay = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(today)

        // Get the year, e.g., "2024"
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(today)

        // Combine the strings, placing the year on a new line
        val dateText = "Today is $dayOfWeek\n$monthDay\n$year"

        // Set the combined text to the dateTextView
        dateTextView.text = dateText
    }
}