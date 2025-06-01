package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.adapter.FavoriteAdapter
import com.example.musicstream.databinding.ActivityFavoriteBinding
import com.example.musicstream.models.FavoriteModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FavoriteActivity : AppCompatActivity() {

    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityFavoriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView)
        favoriteRecyclerView.layoutManager = LinearLayoutManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        setupHeader()
        setupMenu()
        loadFavoriteSongs()
    }
    private fun setupHeader() {
        val optionBtn = findViewById<ImageView>(R.id.option_btn)

        // Show popup menu when option button is clicked
        optionBtn.setOnClickListener {
            showPopupWindow()
        }
    }

    private fun setupMenu() {
        val menuLayout = findViewById<View>(R.id.menu)
        val searchIcon = menuLayout.findViewById<ImageView>(R.id.search_icon)
        val userIcon = menuLayout.findViewById<ImageView>(R.id.user_icon)
        val homeIcon = menuLayout.findViewById<ImageView>(R.id.home_icon)
        val favoriteIcon = menuLayout.findViewById<ImageView>(R.id.favorites_icon)

        // Highlight the current activity's icon
        favoriteIcon.setImageResource(R.drawable.heart_2)

        homeIcon.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        userIcon.setOnClickListener {
            startActivity(Intent(this, UserAccountActivity::class.java))
            finish()
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

    private fun loadFavoriteSongs() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("favorites")
            .whereEqualTo("userUid", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val favoriteModel = document.toObject(FavoriteModel::class.java)
                    val songIdList = favoriteModel?.songs ?: emptyList()

                    if (songIdList.isNotEmpty()) {
                        if (!::favoriteAdapter.isInitialized) {
                            favoriteAdapter = FavoriteAdapter(songIdList.toMutableList()) { songId ->
                                showDeleteConfirmation(songId)
                            }
                            favoriteRecyclerView.adapter = favoriteAdapter
                        } else {
                            favoriteAdapter.songIdList.clear()
                            favoriteAdapter.songIdList.addAll(songIdList)
                            favoriteAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun showDeleteConfirmation(songId: String) {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.delete_confirmation_layout, null)

        val popupWindow = PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)

        // Xử lý nút "Delete"
        view.findViewById<Button>(R.id.delete_button).setOnClickListener {
            deleteSongFromFavorites(songId)
            popupWindow.dismiss()
        }

        // Xử lý nút "Cancel"
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            popupWindow.dismiss()
        }
    }
    private fun deleteSongFromFavorites(songId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("favorites")
            .whereEqualTo("userUid", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val favoriteModel = document.toObject(FavoriteModel::class.java)

                    val updatedSongList = favoriteModel?.songs?.toMutableList()
                    updatedSongList?.remove(songId)

                    // Cập nhật lại danh sách yêu thích
                    db.collection("favorites")
                        .document(document.id)
                        .update("songs", updatedSongList)
                        .addOnSuccessListener {
                            // Cập nhật adapter và RecyclerView
                            if (::favoriteAdapter.isInitialized) {
                                val position = favoriteAdapter.songIdList.indexOf(songId)
                                (favoriteAdapter.songIdList as MutableList).remove(songId)
                                favoriteAdapter.notifyItemRemoved(position)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }


    fun showPopupWindow() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_menu_layout, null)

        val popupWindow = PopupWindow(view, resources.displayMetrics.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.update()
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)

        view.findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            popupWindow.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.home_option).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            popupWindow.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.all_songs_layout).setOnClickListener {
            startActivity(Intent(this, AllSongsActivity::class.java))
            popupWindow.dismiss()
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
