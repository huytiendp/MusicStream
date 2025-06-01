package com.example.musicstream.Admin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicstream.R
import com.example.musicstream.databinding.ActivityCategoryAdminBinding
import com.example.musicstream.models.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore

class CategoryAdminActivity : AppCompatActivity(), OnCategoryClickListener {

    private lateinit var binding: ActivityCategoryAdminBinding
    private lateinit var categoryAdminAdapter: CategoryAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Category Admin"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Return to the previous screen
        }

        setupRecyclerView()
        loadCategories()

        binding.fabAddCategory.setOnClickListener {
            val intent = Intent(this, UploadCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        binding.categoriesAdminRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdminAdapter = CategoryAdminAdapter(listOf(), this) // Pass the listener
        binding.categoriesAdminRecyclerView.adapter = categoryAdminAdapter
    }

    private fun loadCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get()
            .addOnSuccessListener { snapshot ->
                val categoryList = snapshot.toObjects(CategoryModel::class.java)
                categoryAdminAdapter = CategoryAdminAdapter(categoryList, this) // Update adapter with data from Firestore
                binding.categoriesAdminRecyclerView.adapter = categoryAdminAdapter // Set adapter to show new data
            }
            .addOnFailureListener {
                // Handle error if needed
            }
    }

    override fun onCategoryClick(category: CategoryModel) {
        // Handle category click event
        val intent = Intent(this, CategoryDetailAdminActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    override fun onEditClick(category: CategoryModel) {
        // Handle edit click event
        val intent = Intent(this, EditCategoryAdminActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }
}
