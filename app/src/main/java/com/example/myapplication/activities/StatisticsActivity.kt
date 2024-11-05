package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.CycleAdapter
import com.example.myapplication.database.DatabaseHelper

class StatisticsActivity : ComponentActivity() {

    private lateinit var listView: ListView
    private lateinit var cyclesAdapter: CycleAdapter
    private lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val userId = intent.getLongExtra("user_id", -1)
        databaseHelper = DatabaseHelper(this)

        // Handle "Back" button
        val backButton: ImageView = findViewById(R.id.imageBackButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
        }

        // Initialize ListView
        listView = findViewById(R.id.cycleHistoryListView)

        loadCycles(userId)
    }

    private fun loadCycles(userId: Long) {
        // Retrieve data from database
        val cycles = databaseHelper.getAllCycles(userId) // Assumes it returns List<Cycle>

        // Set up adapter for ListView
        cyclesAdapter = CycleAdapter(this, cycles)
        listView.adapter = cyclesAdapter
    }
}
