package com.example.personalapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.personalapp.fragments.HomeFragment
import com.example.personalapp.fragments.NotesFragment
import com.example.personalapp.fragments.TodoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val notesFragment = NotesFragment()
        val todoFragment = TodoFragment()

        // Initialize the BottomNavigationView
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        makeCurrentFragment(homeFragment) // Show the home fragment

        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId)
            {
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.todo -> makeCurrentFragment(todoFragment)
                R.id.notes -> makeCurrentFragment(notesFragment)
                else -> false
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}