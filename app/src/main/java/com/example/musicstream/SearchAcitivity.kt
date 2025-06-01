package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.adapter.SongAdapter
import com.example.musicstream.databinding.ActivitySearchBinding
import com.example.musicstream.models.SongModel
import com.example.musicstream.repositories.SongRepository
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding // Define binding variable
    private lateinit var songAdapter: SongAdapter
    private lateinit var songRepository: SongRepository
    private lateinit var menuLayout: View // Variable for menu_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Initialize view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view using binding

        // Inflate the menu layout
        val inflater = LayoutInflater.from(this)
        menuLayout = inflater.inflate(R.layout.menu_layout, null)
        val homeIcon = binding.root.findViewById<ImageView>(R.id.home_icon)
        val userIcon = binding.root.findViewById<ImageView>(R.id.user_icon)
        val favoriteIcon = binding.root.findViewById<ImageView>(R.id.favorites_icon)
        val searchIcon = binding.root.findViewById<ImageView>(R.id.search_icon)
        searchIcon.setImageResource(R.drawable.search_3)


        // Set up option button click listener to show popup
        binding.optionBtn.setOnClickListener {
            showPopupWindow()
        }

        // Initialize repository and adapter
        songRepository = SongRepository()
        songAdapter = SongAdapter(listOf(), this)

        // Set up RecyclerView
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecyclerView.adapter = songAdapter

        // Add a listener to the search input field
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                performSearch(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.root.findViewById<ImageView>(R.id.camera_input).setOnClickListener {
            val intent = Intent(this, QRScannerActivity::class.java)
            startActivity(intent)
        }

        // Handle click on the home icon to navigate to MainActivity
        homeIcon.setOnClickListener {
            val homeIntent = Intent(this, MainActivity::class.java)
            startActivity(homeIntent)
        }
        userIcon.setOnClickListener {
            val userIntent = Intent(this, UserAccountActivity::class.java)
            startActivity(userIntent)
        }
        favoriteIcon.setOnClickListener {
            val searchIntent = Intent(this, FavoriteActivity::class.java)
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

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            // Search for songs using the repository and update the adapter with the results
            val results: List<SongModel> = songRepository.searchSongs(query)
            songAdapter.updateSongs(results)
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

        view.findViewById<LinearLayout>(R.id.all_songs_layout).setOnClickListener {
            startActivity(Intent(this, AllSongsActivity::class.java))  // Redirect to AllSongsActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }
        view.findViewById<LinearLayout>(R.id.playlist_layout).setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))  // Redirect to AllSongsActivity
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
