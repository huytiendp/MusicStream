package com.example.musicstream.Admin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.R
import com.example.musicstream.Admin.ReportAdminActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Khởi tạo các nút
        val btnCategories = findViewById<Button>(R.id.btnCategories)
        val btnSections = findViewById<Button>(R.id.btnSections)
        val btnSongs = findViewById<Button>(R.id.btnSongs)
        val btnUsers = findViewById<Button>(R.id.btnUsers)
        val btnReports = findViewById<Button>(R.id.btnReports)



        // Xử lý sự kiện điều hướng
        btnCategories.setOnClickListener {
            startActivity(Intent(this, CategoryAdminActivity::class.java))
        }

        btnSections.setOnClickListener {
            startActivity(Intent(this, SectionAdminActivity::class.java))
        }

        btnSongs.setOnClickListener {
            startActivity(Intent(this, SongsAdminActivity::class.java))
        }

        btnUsers.setOnClickListener {
            startActivity(Intent(this, UserAdminActivity::class.java))
        }
        btnReports.setOnClickListener {
            startActivity(Intent(this, ReportAdminActivity::class.java))
        }

    }
}
