package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.adapter.PlaylistAdapter
import com.example.musicstream.models.PlaylistModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var addPlaylistFab: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()
    private val playlists = mutableListOf<PlaylistModel>()
    private var isSelectionMode = false
    private lateinit var optionMenu: ImageView
    private lateinit var selectionModeLayout: LinearLayout
    private lateinit var backButton: View
    private lateinit var selectionCount: TextView
    private lateinit var deleteButton: Button

    companion object {
        const val REQUEST_ADD_PLAYLIST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        optionMenu = findViewById(R.id.option_btn)
        selectionModeLayout = findViewById(R.id.selectionModeLayout)
        backButton = findViewById(R.id.back_button)
        deleteButton = findViewById(R.id.delete_button)

        // Enable menu selection
        optionMenu.setOnClickListener {
            enterSelectionMode()
        }

        // Exit selection mode
        backButton.setOnClickListener {
            exitSelectionMode()
        }

        // Set up custom header with exit button
        val exitButton = findViewById<View>(R.id.exit_button)
        exitButton.setOnClickListener {
            finish() // Close the activity when exit button is pressed
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        deleteButton.setOnClickListener {
            val selectedPlaylists = playlistAdapter.getSelectedPlaylists()

            if (selectedPlaylists.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn xóa các playlist đã chọn?")
                    .setPositiveButton("Xóa") { _, _ ->
                        for (playlist in selectedPlaylists) {
                            db.collection("playlists").document(playlist.id)
                                .delete()
                                .addOnSuccessListener {
                                    playlists.remove(playlist)
                                    playlistAdapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    // Hiển thị lỗi nếu xóa thất bại
                                }
                        }
                        exitSelectionMode()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }

        playlistRecyclerView = findViewById(R.id.playlistRecyclerView)
        addPlaylistFab = findViewById(R.id.addPlaylistFab)

        // Set up RecyclerView
        playlistAdapter = PlaylistAdapter(playlists, this)
        playlistRecyclerView.adapter = playlistAdapter
        playlistRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load playlists
        loadPlaylists()

        // Open AddPlaylistActivity to add a new playlist
        addPlaylistFab.setOnClickListener {
            val intent = Intent(this, AddPlaylistActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_PLAYLIST)
        }
    }

    private fun loadPlaylists() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("playlists")
            .whereEqualTo("userUid", uid)
            .get()
            .addOnSuccessListener { documents ->
                playlists.clear()
                for (document in documents) {
                    val playlist = document.toObject(PlaylistModel::class.java)
                    playlists.add(playlist)
                }
                playlistAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Handle error here
            }
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        selectionModeLayout.visibility = View.VISIBLE
        playlistAdapter = PlaylistAdapter(playlists, this, true) // Pass `isSelectionMode=true`
        playlistRecyclerView.adapter = playlistAdapter
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectionModeLayout.visibility = View.GONE
        playlistAdapter = PlaylistAdapter(playlists, this, false)
        playlistRecyclerView.adapter = playlistAdapter
    }

    private fun updateSelectionCount() {
        val count = playlistAdapter.getSelectedPlaylists().size
        selectionCount.text = "$count selected"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_PLAYLIST && resultCode == RESULT_OK) {
            val newPlaylist = data?.getSerializableExtra("newPlaylist") as? PlaylistModel
            newPlaylist?.let {
                playlists.add(it)
                playlistAdapter.notifyItemInserted(playlists.size - 1)
            }
        }
    }
}
