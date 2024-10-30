package com.example.myapplication.activities
import CycleAdapter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper

class StatisticsActivity : AppCompatActivity() {

    private lateinit var cycleHistoryListView: ListView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_statistics)
        val userId = intent.getLongExtra("user_id", -1)

        cycleHistoryListView = findViewById(R.id.cycleHistoryListView)
        databaseHelper = DatabaseHelper(this)

        loadCycleData(userId)
    }

    private fun loadCycleData(userId: Long) {
        // Получите данные о циклах из базы данных
        val cycles = databaseHelper.getCycleHistory(userId) // Метод для получения истории циклов
        val adapter = CycleAdapter(this, cycles)

        cycleHistoryListView.adapter = adapter
    }
}
