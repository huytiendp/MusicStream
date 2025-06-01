package com.example.musicstream.Admin

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicstream.R
import com.example.musicstream.adapter.SongSelectionAdapter
import com.example.musicstream.databinding.ActivitySectionDetailAdminBinding
import com.example.musicstream.databinding.DialogSelectSongBinding
import com.example.musicstream.models.SectionModel
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SectionDetailAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySectionDetailAdminBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var sectionId: String
    private var selectedSongs = mutableListOf<String>() // Lưu trữ các bài hát đã chọn
    private var existingSongs = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySectionDetailAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Section Details"

        // Handle Back Button click
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Get section ID from Intent
        sectionId = intent.getStringExtra("sectionId") ?: return

        // Fetch section details
        fetchSectionDetails(sectionId)
    }

    // Inflate the menu to add items to the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_section_admin_menu, menu) // Inflate menu with "Add Song"
        return true
    }

    // Handle item selection in the toolbar menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_song -> {
                showSongSelectionDialog() // Show dialog to select songs
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchSectionDetails(sectionId: String) {
        db.collection("sections").document(sectionId).get()
            .addOnSuccessListener { document ->
                val section = document.toObject(SectionModel::class.java)
                section?.let {
                    existingSongs = it.songs.toMutableList() // Store existing songs list
                    displaySectionDetails(it)

                    // Fetch and display song titles using RecyclerView
                    db.collection("songs").whereIn("id", it.songs).get()
                        .addOnSuccessListener { snapshot ->
                            val songModels = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(SongModel::class.java)
                            }
                            displaySongsInRecyclerView(songModels)
                        }
                }
            }
    }

    private fun displaySectionDetails(section: SectionModel) {
        binding.sectionNameTextView.text = section.name
        Glide.with(this).load(section.coverUrl).into(binding.sectionCoverImageView)
    }

    private fun displaySongsInRecyclerView(songModels: List<SongModel>) {
        // Setup RecyclerView to display songs
        val adapter = SongListAdminAdapter(songModels)
        binding.songRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songRecyclerView.adapter = adapter
    }

    private fun showSongSelectionDialog() {
        val dialogBinding = DialogSelectSongBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        // Set up RecyclerView for song selection
        db.collection("songs").get().addOnSuccessListener { snapshot ->
            val songs = snapshot.documents.mapNotNull { it.toObject(SongModel::class.java) }
            selectedSongs = existingSongs.toMutableList() // Initialize with existing songs
            val adapter = SongSelectionAdapter(songs, selectedSongs)

            dialogBinding.songRecyclerView.layoutManager = LinearLayoutManager(this)
            dialogBinding.songRecyclerView.adapter = adapter

            // Listen for changes in song selection
            adapter.setOnSongSelectionChanged { updatedList ->
                selectedSongs = updatedList.toMutableList()
            }
        }

        // Add button to save selected songs
        val addButton = Button(this).apply {
            text = "Add Songs"
            setOnClickListener {
                if (selectedSongs.isNotEmpty()) {
                    addSongsToSection()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@SectionDetailAdminActivity, "No songs selected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Add the button programmatically to the dialog layout
        (dialogBinding.root as LinearLayout).addView(addButton)

        dialog.show()
    }

    private fun addSongsToSection() {
        val updatedSongs = selectedSongs.distinct() // Loại bỏ trùng lặp

        db.collection("sections").document(sectionId).update("songs", updatedSongs)
            .addOnSuccessListener {
                Toast.makeText(this, "Songs updated successfully", Toast.LENGTH_SHORT).show()

                // Cập nhật trực tiếp danh sách bài hát hiển thị
                db.collection("songs").whereIn("id", updatedSongs).get()
                    .addOnSuccessListener { snapshot ->
                        val songModels = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(SongModel::class.java)
                        }
                        // Cập nhật dữ liệu cho RecyclerView
                        updateRecyclerView(songModels)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update songs", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRecyclerView(songModels: List<SongModel>) {
        val adapter = SongListAdminAdapter(songModels)
        binding.songRecyclerView.adapter = adapter
    }


}
