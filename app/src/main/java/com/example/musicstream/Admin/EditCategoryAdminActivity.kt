package com.example.musicstream.Admin

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.R
import com.example.musicstream.models.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore

class EditCategoryAdminActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var coverUrlEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var category: CategoryModel
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category_admin)

        // Setup Toolbar with Back Button
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Category"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Handle Back Button click
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Go back to the previous screen
        }

        // Nhận CategoryModel từ Intent
        category = intent.getSerializableExtra("category") as CategoryModel

        // Khởi tạo các view
        nameEditText = findViewById(R.id.nameEditText)
        coverUrlEditText = findViewById(R.id.coverUrlEditText)
        saveButton = findViewById(R.id.saveButton)

        // Hiển thị thông tin hiện tại
        populateFields(category)

        // Lưu thay đổi
        saveButton.setOnClickListener {
            updateCategory()
        }
    }

    private fun populateFields(category: CategoryModel) {
        nameEditText.setText(category.name)
        coverUrlEditText.setText(category.coverUrl)
    }

    private fun updateCategory() {
        val newName = nameEditText.text.toString()
        val newCoverUrl = coverUrlEditText.text.toString()

        val categoryId = category.name
        val updatedCategory = mapOf("name" to newName, "coverUrl" to newCoverUrl)

        db.collection("category").document(categoryId)
            .update(updatedCategory)
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener {
                // Xử lý lỗi nếu cần
            }
    }
}
