package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.adapter.SongAdapter
import com.example.musicstream.databinding.ActivityDetailSingerBinding
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class DetailSingerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailSingerBinding
    private lateinit var adapter: SongAdapter
    private val allSongs = mutableListOf<SongModel>() // Lưu tất cả bài hát

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSingerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.exitButton.setOnClickListener {
            finish() // Đóng activity khi nút thoát được bấm
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Nhận dữ liệu từ Intent
        val singerName = intent.getStringExtra("singerName")
        val singerImageUrl = intent.getStringExtra("singerImageUrl")
        val singerDetail = intent.getStringExtra("singerDetail")

        // Hiển thị thông tin ca sĩ
        binding.singerNameTextView.text = singerName
        binding.singerDetailTextView.text = singerDetail
        Glide.with(this).load(singerImageUrl).into(binding.singerImageView)

        // Cấu hình RecyclerView
        adapter = SongAdapter(emptyList(), this)
        binding.singerSongsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.singerSongsRecyclerView.adapter = adapter

        // Lấy dữ liệu từ Firebase
        fetchAllSongsFromFirebase { songs ->
            allSongs.clear()
            allSongs.addAll(songs)

            // Lọc bài hát theo tên ca sĩ
            val relatedSongs = allSongs.filter { it.subtitle == singerName }
            adapter.updateSongs(relatedSongs) // Cập nhật dữ liệu cho RecyclerView
        }
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        binding.pauseButton.setOnClickListener {
            // Check if ExoPlayer is currently playing and pause it
            MyExoplayer.getInstance()?.let { exoPlayer ->
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()  // Pause the playback
                    binding.pauseButton.setImageResource(R.drawable.play_arrow)  // Optionally, change the icon to a "play" button
                } else {
                    exoPlayer.play()  // Resume the playback
                    binding.pauseButton.setImageResource(R.drawable.pause)  // Change icon back to "pause"
                }
            }
        }

    }

    private fun fetchAllSongsFromFirebase(onResult: (List<SongModel>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("songs").get()
            .addOnSuccessListener { documents ->
                val songs = documents.mapNotNull { it.toObject<SongModel>() }
                Log.d("Firebase", "Fetched ${songs.size} songs")
                onResult(songs)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching songs", exception)
                Toast.makeText(this, "Không thể tải dữ liệu!", Toast.LENGTH_SHORT).show()
                onResult(emptyList())
            }
    }

    override fun onResume() {
        super.onResume()
        showPlayerView()
    }

    fun showPlayerView() {
        binding.playerView.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top) // Áp dụng animation
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        } ?: run {
            binding.playerView.visibility = View.GONE
        }
    }

}
