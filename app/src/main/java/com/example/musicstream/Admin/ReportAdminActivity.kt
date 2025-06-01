package com.example.musicstream.Admin

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.R
import com.example.musicstream.adapter.ReportAdapter
import com.example.musicstream.models.ReportModel
import com.google.firebase.firestore.FirebaseFirestore

class ReportAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_admin)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Thiết lập Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút quay lại

        toolbar.setNavigationOnClickListener {
            finish() // Thoát Activity và quay lại màn hình trước
        }

        // Thiết lập RecyclerView
        recyclerView = findViewById(R.id.reportsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReportAdapter(listOf())
        recyclerView.adapter = adapter

        fetchReports()
    }

    private fun fetchReports() {
        val db = FirebaseFirestore.getInstance()
        db.collection("reports")
            .get()
            .addOnSuccessListener { documents ->
                val reports = documents.map { doc ->
                    ReportModel(
                        songId = doc.getString("songId") ?: "",
                        songTitle = doc.getString("songTitle") ?: "",
                        reason = doc.getString("reason") ?: "",
                        reportedAt = doc.getTimestamp("reportedAt")?.toDate().toString(),
                        reportedBy = doc.getString("reportedBy") ?: "",
                        email = doc.getString("email") ?: ""
                    )
                }
                adapter.updateData(reports)
            }
            .addOnFailureListener { e ->
                Log.e("ReportAdmin", "Lỗi khi lấy báo cáo: $e")
            }
    }
}