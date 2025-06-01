package com.example.musicstream.Admin

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.R
import com.example.musicstream.models.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore

class UploadCategoryActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var coverUrlEditText: EditText
    private lateinit var saveButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_category)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Cấu hình Toolbar và nút quay lại
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Thêm nút quay lại vào Toolbar

        // Xử lý sự kiện quay lại khi nhấn vào nút
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Khởi tạo các view
        nameEditText = findViewById(R.id.nameEditText)
        coverUrlEditText = findViewById(R.id.coverUrlEditText)
        saveButton = findViewById(R.id.saveButton)

        // Xử lý sự kiện khi nhấn nút Save
        saveButton.setOnClickListener {
            uploadCategory()
        }
    }

    private fun uploadCategory() {
        val name = nameEditText.text.toString().trim()
        val coverUrl = coverUrlEditText.text.toString().trim()

        if (name.isEmpty() || coverUrl.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo một đối tượng CategoryModel từ các giá trị người dùng nhập
        val newCategory = CategoryModel(name, coverUrl, listOf())

        // Lưu danh mục vào Firestore
        db.collection("category")
            .document(name) // Sử dụng tên làm ID
            .set(newCategory)
            .addOnSuccessListener {
                Toast.makeText(this, "Category uploaded successfully", Toast.LENGTH_SHORT).show()
                finish() // Đóng activity sau khi upload
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
