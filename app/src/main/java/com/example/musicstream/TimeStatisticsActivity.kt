package com.example.musicstream

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.adapter.TimeStatisticsAdapter
import com.example.musicstream.models.UsageStatisticsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TimeStatisticsActivity : AppCompatActivity() {

    private lateinit var totalTimeTextView: TextView
    private lateinit var statisticsRecyclerView: RecyclerView
    private lateinit var adapter: TimeStatisticsAdapter
    private var sessionStartTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_statistics)

        totalTimeTextView = findViewById(R.id.totalTimeTextView)
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView)

        // Setup RecyclerView
        adapter = TimeStatisticsAdapter(emptyList())
        statisticsRecyclerView.layoutManager = LinearLayoutManager(this)
        statisticsRecyclerView.adapter = adapter

        // Set up session start
        sessionStartTime = System.currentTimeMillis()

        // Fetch statistics data on create
        fetchUsageStatistics()
    }

    override fun onStop() {
        super.onStop()
        saveUsageTime()
    }

    /**
     * Lưu thống kê dữ liệu người dùng và thời gian sử dụng khi session kết thúc.
     */
    private fun saveUsageTime() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val sessionEndTime = System.currentTimeMillis()
        val usageTime = sessionEndTime - sessionStartTime

        val data = hashMapOf(
            "userId" to userId,
            "timestamp" to sessionStartTime,
            "usageTime" to usageTime
        )

        db.collection("usage_stats")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm thống kê thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không thể lưu dữ liệu!", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Lấy dữ liệu thống kê người dùng từ Firestore và xử lý dữ liệu.
     */
    private fun fetchUsageStatistics() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usage_stats")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("Firestore", "Documents fetched: ${documents.size()}") // Debug

                val statisticsList = mutableListOf<UsageStatisticsModel>()

                for (document in documents) {
                    val usageTime = document.getLong("usageTime") ?: 0L
                    val timestamp = document.getLong("timestamp") ?: 0L

                    statisticsList.add(UsageStatisticsModel(timestamp, usageTime))
                }

                Log.d("Firestore", "Statistics List: $statisticsList") // Debug

                // Send data to adapter
                adapter.submitList(statisticsList)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching data", e)
                Toast.makeText(this, "Không thể lấy dữ liệu thống kê!", Toast.LENGTH_SHORT).show()
            }
    }
}
