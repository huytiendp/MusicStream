package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.adapter.SongAdapter
import com.example.musicstream.models.PlaylistModel
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SongPlaylistActivity : AppCompatActivity() {

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val db = FirebaseFirestore.getInstance()
    private val songs = mutableListOf<SongModel>()
    private lateinit var playlistId: String

    companion object {
        private const val REQUEST_SELECT_SONGS = 100 // Mã định danh yêu cầu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_playlist)

        // Get playlist ID from Intent
        playlistId = intent.getStringExtra("playlistId") ?: return // Nếu không có playlistId, thoát Activity

        // Thiết lập Floating Action Button
        val addPlaylistFab = findViewById<View>(R.id.addPlaylistFab)
        addPlaylistFab.setOnClickListener {
            val intent = Intent(this, SelectSongsActivity::class.java)
            intent.putExtra("playlistId", playlistId) // Truyền playlistId sang SelectSongsActivity
            intent.putStringArrayListExtra("selectedSongs", ArrayList(songs.map { it.id }))
            startActivityForResult(intent, REQUEST_SELECT_SONGS) // Khởi động SelectSongsActivity
        }

        // Thiết lập nút exit
        val exitButton = findViewById<View>(R.id.exit_button)
        exitButton.setOnClickListener {
            finish() // Đóng Activity khi nhấn nút exit
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Khởi tạo RecyclerView
        songsRecyclerView = findViewById(R.id.songsRecyclerView)
        songAdapter = SongAdapter(songs, this)
        songsRecyclerView.adapter = songAdapter
        songsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Tải danh sách bài hát của playlist
        loadSongs()
    }

    private fun loadSongs() {
        db.collection("playlists").document(playlistId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val playlist = document.toObject(PlaylistModel::class.java)
                    val songIds = playlist?.songs ?: emptyList()

                    if (songIds.isEmpty()) {
                        songs.clear()
                        songAdapter.notifyDataSetChanged()
                        return@addOnSuccessListener
                    }

                    db.collection("songs").whereIn("id", songIds)
                        .get()
                        .addOnSuccessListener { songDocuments ->
                            songs.clear()
                            for (songDoc in songDocuments) {
                                val song = songDoc.toObject(SongModel::class.java)
                                songs.add(song)
                            }
                            songAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            // Handle error here
                        }
                }
            }
            .addOnFailureListener {
                // Handle error here
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Kiểm tra kết quả từ SelectSongsActivity
        if (requestCode == REQUEST_SELECT_SONGS && resultCode == RESULT_OK) {
            loadSongs() // Reload danh sách bài hát
        }
    }
}
