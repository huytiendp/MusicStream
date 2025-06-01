package com.example.musicstream.Admin

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.R
import com.example.musicstream.databinding.ActivityEditSongAdminBinding
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class EditSongAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditSongAdminBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var songId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSongAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Song"

        // Handle Back Button click
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        songId = intent.getStringExtra("songId") ?: return
        loadSongDetails(songId)

        binding.saveSongButton.setOnClickListener {
            updateSong()
        }
    }

    private fun loadSongDetails(songId: String) {
        db.collection("songs").document(songId).get()
            .addOnSuccessListener { snapshot ->
                val song = snapshot.toObject(SongModel::class.java)
                song?.let { populateFields(it) }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không thể tải thông tin bài hát", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFields(song: SongModel) {
        binding.editSongTitle.setText(song.title)
        binding.editSongSubtitle.setText(song.subtitle)
        binding.editSongUrl.setText(song.url)
        binding.editSongCoverUrl.setText(song.coverUrl)
        binding.editSongLyrics.setText(song.lyric)
        binding.editSongSingerUrl.setText(song.singerUrl)
        binding.editSongDetailSinger.setText(song.detailSinger)
    }

    private fun updateSong() {
        val updatedSong = SongModel(
            id = songId,
            title = binding.editSongTitle.text.toString(),
            subtitle = binding.editSongSubtitle.text.toString(),
            url = binding.editSongUrl.text.toString(),
            coverUrl = binding.editSongCoverUrl.text.toString(),
            lyric = binding.editSongLyrics.text.toString(),
            singerUrl = binding.editSongSingerUrl.text.toString(),
            detailSinger = binding.editSongDetailSinger.text.toString()
        )

        db.collection("songs").document(songId)
            .set(updatedSong)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật bài hát thành công", Toast.LENGTH_SHORT).show()
                finish() // Đóng Activity sau khi lưu
            }
            .addOnFailureListener {
                Toast.makeText(this, "Cập nhật bài hát thất bại", Toast.LENGTH_SHORT).show()
            }
    }
}
