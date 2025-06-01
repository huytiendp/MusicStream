package com.example.musicstream.Admin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicstream.R
import com.example.musicstream.databinding.ActivitySongsAdminBinding
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SongsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongsAdminBinding
    private lateinit var songAdapter: SongAdminAdapter
    private val songList = mutableListOf<SongModel>()
    private val filteredSongList = mutableListOf<SongModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Songs Admin"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Return to the previous screen
        }

        setupRecyclerView()
        loadSongsFromFirestore()

        // Set up click listener for the Floating Action Button
        binding.addSongFab.setOnClickListener {
            startActivity(Intent(this, UploadMusicActivity::class.java))
        }

        // Call setup search functionality
        setupSearchFunctionality()
    }

    private fun setupRecyclerView() {
        binding.songsAdminRecyclerView.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdminAdapter(filteredSongList)  // Use filteredSongList
        binding.songsAdminRecyclerView.adapter = songAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_songs_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sort_by_count -> {
                // Sort the song list by count in descending order
                songList.sortByDescending { it.count }
                filterSongs(binding.searchEditText.text.toString()) // Apply current filter after sorting
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSongsFromFirestore() {
        FirebaseFirestore.getInstance().collection("songs")
            .get()
            .addOnSuccessListener { snapshot ->
                val songs = snapshot.toObjects(SongModel::class.java)
                songList.clear()
                songList.addAll(songs)
                filteredSongList.clear()
                filteredSongList.addAll(songs)
                songAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Handle any errors here, such as showing a toast message
            }
    }

    private fun setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSongs(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterSongs(query: String) {
        val lowerCaseQuery = query.toLowerCase()
        filteredSongList.clear()
        if (query.isEmpty()) {
            filteredSongList.addAll(songList)
        } else {
            val filteredResults = songList.filter {
                it.title.toLowerCase().contains(lowerCaseQuery)
            }
            filteredSongList.addAll(filteredResults)
        }
        songAdapter.notifyDataSetChanged() // Notify adapter to update the RecyclerView
    }
}
