package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.MyExoplayer.extractUniqueSingers
import com.example.musicstream.adapter.SingerAdapter
import com.example.musicstream.databinding.ActivitySingerBinding
import com.example.musicstream.models.SingerModel
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class SingerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingerBinding
    private lateinit var singerAdapter: SingerAdapter
    private lateinit var originalSingerList: List<SingerModel> // Danh sách đầy đủ ca sĩ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivitySingerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
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

        // Set up exit button listener
        binding.exitButton.setOnClickListener {
            finish() // Close activity when exit button is clicked
        }

        // Set up search functionality
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSingers(s.toString()) // Filter list based on input
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fetchSongList()
    }

    private fun fetchSongList() {
        val db = FirebaseFirestore.getInstance()
        db.collection("songs")
            .get()
            .addOnSuccessListener { snapshot ->
                val songs = snapshot.documents.mapNotNull { document ->
                    document.toObject(SongModel::class.java)
                }
                val uniqueSingers = extractUniqueSingers(songs)
                originalSingerList = uniqueSingers // Lưu danh sách đầy đủ
                setUpRecyclerView(uniqueSingers)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Error fetching song list", e)
            }
    }

    private fun setUpRecyclerView(uniqueSingers: List<SingerModel>) {
        singerAdapter = SingerAdapter(uniqueSingers, this) // Pass context
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = singerAdapter
    }

    private fun filterSingers(query: String?) {
        if (query.isNullOrBlank()) {
            setUpRecyclerView(originalSingerList) // Hiển thị danh sách gốc nếu không có input
        } else {
            val filteredList = originalSingerList.filter {
                it.name.contains(query, ignoreCase = true) // Tìm kiếm không phân biệt hoa thường
            }
            setUpRecyclerView(filteredList) // Cập nhật RecyclerView với danh sách đã lọc
        }
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
    override fun onResume() {
        super.onResume()
        showPlayerView()
    }

}
