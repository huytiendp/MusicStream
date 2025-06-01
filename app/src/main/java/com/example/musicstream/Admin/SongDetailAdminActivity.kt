package com.example.musicstream.Admin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicstream.R
import com.example.musicstream.databinding.ActivitySongDetailAdminBinding
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SongDetailAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongDetailAdminBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongDetailAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Song Details"

        // Handle Back Button click
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Go back to the previous screen
        }

        val songId = intent.getStringExtra("songId")
        loadSongDetails(songId)

        // Xử lý sự kiện cho nút sửa
        binding.editSongButton.setOnClickListener {
            songId?.let {
                val intent = Intent(this, EditSongAdminActivity::class.java)
                intent.putExtra("songId", it)
                startActivity(intent)
            }
        }

        // Xử lý sự kiện cho nút xóa
        binding.deleteSongButton.setOnClickListener {
            songId?.let { id ->
                showDeleteConfirmationDialog(id)
            }
        }

    }

    private fun loadSongDetails(songId: String?) {
        songId?.let {
            db.collection("songs")
                .document(it)
                .get()
                .addOnSuccessListener { snapshot ->
                    val song = snapshot.toObject(SongModel::class.java)
                    song?.let { displaySongDetails(it) }
                }
        }
    }

    private fun displaySongDetails(song: SongModel) {
        binding.songIdTextView.text = "ID: ${song.id}"
        binding.songTitleTextView.text = song.title
        binding.songSubtitleTextView.text = song.subtitle
        binding.songUrlTextView.text = "Link bài hát: ${song.url}"
        binding.songLyricsTextView.text ="Lời bài hát: ${song.lyric}"
        binding.songSingerUrlTextView.text = "Link ảnh ca sĩ: ${song.singerUrl}"
        binding.songDetailSingerTextView.text = "Thông tin nghệ sĩ: ${song.detailSinger}"
        binding.songCountTextView.text = "Lượt Nghe: ${song.count}"

        Glide.with(this).load(song.coverUrl).into(binding.songCoverImageView)
    }

    private fun showDeleteConfirmationDialog(songId: String) {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa bài hát này không?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteSong(songId)
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteSong(songId: String) {
        db.collection("songs")
            .document(songId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Xóa bài hát thành công", Toast.LENGTH_SHORT).show()
                finish()  // Đóng Activity sau khi xóa
            }
            .addOnFailureListener {
                Toast.makeText(this, "Xóa bài hát thất bại", Toast.LENGTH_SHORT).show()
            }
    }
}
