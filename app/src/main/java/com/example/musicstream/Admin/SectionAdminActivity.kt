package com.example.musicstream.Admin

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicstream.R
import com.example.musicstream.databinding.ActivitySectionAdminBinding
import com.google.firebase.firestore.FirebaseFirestore

class SectionAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySectionAdminBinding
    private val sectionIdList = mutableListOf<String>()
    private lateinit var adapter: SectionAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySectionAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Section Admin"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Return to the previous screen
        }

        // Initialize RecyclerView
        adapter = SectionAdminAdapter(sectionIdList)
        binding.sectionRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.sectionRecyclerView.adapter = adapter

        // Fetch sections from Firestore
        fetchSectionsFromFirestore()
    }

    private fun fetchSectionsFromFirestore() {
        FirebaseFirestore.getInstance().collection("sections")
            .get()
            .addOnSuccessListener { documents ->
                sectionIdList.clear()
                for (document in documents) {
                    document.id?.let { sectionIdList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
