package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.adapter.SongAdapter
import com.example.musicstream.databinding.ActivityAllSongsBinding
import com.example.musicstream.models.SongModel
import com.example.musicstream.repositories.SongRepositories
import kotlinx.coroutines.launch

class AllSongsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllSongsBinding
    private lateinit var songAdapter: SongAdapter
    private lateinit var songRepository: SongRepositories
    private lateinit var menuLayout: View // Variable for menu_layout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Initialize view binding
        binding = ActivityAllSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inflater = LayoutInflater.from(this)
        menuLayout = inflater.inflate(R.layout.menu_layout, null)
        val homeIcon = binding.root.findViewById<ImageView>(R.id.home_icon)
        val userIcon = binding.root.findViewById<ImageView>(R.id.user_icon)
        val searchIcon = binding.root.findViewById<ImageView>(R.id.search_icon)
        // Initialize repository and adapter
        songRepository = SongRepositories()
        songAdapter = SongAdapter(listOf(), this)

        // Set up RecyclerView
        binding.allSongsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.allSongsRecyclerView.adapter = songAdapter

        // Load all songs asynchronously
        loadAllSongs()

        binding.exitButton.setOnClickListener {
            finish() // Đóng activity khi nút thoát được bấm
        }
        // Set up option button click listener (Move this after binding is initialized)
        binding.optionBtn.setOnClickListener {
            showPopupWindow()
        }
        homeIcon.setOnClickListener {
            val homeIntent = Intent(this, MainActivity::class.java)
            startActivity(homeIntent)
        }
        userIcon.setOnClickListener {
            val userIntent = Intent(this, UserAccountActivity::class.java)
            startActivity(userIntent)
        }
        searchIcon.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
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


    private fun loadAllSongs() {
        lifecycleScope.launch {
            val allSongs: List<SongModel> = songRepository.getAllSongs()
            val sortedSongs = allSongs.sortedBy { it.title }
            songAdapter.updateSongs(sortedSongs)
        }
    }

    fun showPopupWindow() {
        // Inflate layout from XML
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_menu_layout, null)

        val popupWindow = PopupWindow(view, resources.displayMetrics.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Set properties for PopupWindow
        popupWindow.isFocusable = true
        popupWindow.update()

        // Áp dụng cả enter và exit animation cho PopupWindow
        popupWindow.animationStyle = R.style.PopupAnimation

        // Show the PopupWindow at the bottom of the screen with animation
        popupWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)

        // Handle click events for menu options
        view.findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            // Open SettingsActivity when the settings option is clicked
            startActivity(Intent(this, SettingsActivity::class.java))
            popupWindow.dismiss()  // Close the popup after clicking
        }
        view.findViewById<LinearLayout>(R.id.home_option).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))  // Redirect to MainActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }

        view.findViewById<TextView>(R.id.all_songs).setOnClickListener {
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
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

