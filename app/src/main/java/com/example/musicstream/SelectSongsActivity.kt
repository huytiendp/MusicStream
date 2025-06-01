package com.example.musicstream

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.adapter.SongSelectionAdapter
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SelectSongsActivity : AppCompatActivity() {

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var doneButton: Button
    private lateinit var songSelectionAdapter: SongSelectionAdapter
    private val db = FirebaseFirestore.getInstance()
    private val songs = mutableListOf<SongModel>()
    private val selectedSongs = mutableListOf<String>() // Lưu trữ ID của các bài hát được chọn
    private lateinit var playlistId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_songs)

        playlistId = intent.getStringExtra("playlistId") ?: return
        val previouslySelectedSongs = intent.getStringArrayListExtra("selectedSongs")
        if (previouslySelectedSongs != null) {
            selectedSongs.addAll(previouslySelectedSongs)
        }

        songsRecyclerView = findViewById(R.id.songsRecyclerView)
        doneButton = findViewById(R.id.doneButton)

        // Thiết lập RecyclerView
        songSelectionAdapter = SongSelectionAdapter(songs, selectedSongs)
        songsRecyclerView.adapter = songSelectionAdapter
        songsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Tải danh sách bài hát
        loadSongs()

        // Xử lý khi nhấn nút "Hoàn tất"
        doneButton.setOnClickListener {
            addSongsToPlaylist()
        }
    }

    private fun loadSongs() {
        db.collection("songs").get()
            .addOnSuccessListener { documents ->
                songs.clear()
                for (document in documents) {
                    val song = document.toObject(SongModel::class.java)
                    songs.add(song)
                }
                songSelectionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Handle error here
            }
    }

    private fun addSongsToPlaylist() {
        db.collection("playlists").document(playlistId)
            .update("songs", selectedSongs) // Cập nhật danh sách bài hát vào Firestore
            .addOnSuccessListener {
                // Trả kết quả OK về SongPlaylistActivity
                setResult(RESULT_OK)
                finish() // Đóng SelectSongsActivity
            }
            .addOnFailureListener {
                // Xử lý lỗi ở đây nếu có
            }
    }
}
