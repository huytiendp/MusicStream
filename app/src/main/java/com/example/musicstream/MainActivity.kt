package com.example.musicstream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.Admin.AdminActivity
import com.example.musicstream.adapter.CategoryAdapter
import com.example.musicstream.adapter.SectionSongListAdapter
import com.example.musicstream.adapter.SingerMainAdapter
import com.example.musicstream.databinding.ActivityMainBinding
import com.example.musicstream.models.CategoryModel
import com.example.musicstream.models.SingerModel
import com.example.musicstream.models.SongModel
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val songId = deepLink.getQueryParameter("id")
                    if (!songId.isNullOrEmpty()) {
                        // Chuyển đến PlayerActivity
                        val playerIntent = Intent(this, PlayerActivity::class.java).apply {
                            data = deepLink
                        }
                        startActivity(playerIntent)
                        finish() // Đóng MainActivity
                    }
                }
            }


        val menuLayout = findViewById<View>(R.id.menu)
        val searchIcon = menuLayout.findViewById<ImageView>(R.id.search_icon)
        val userIcon = menuLayout.findViewById<ImageView>(R.id.user_icon)
        val homeIcon = menuLayout.findViewById<ImageView>(R.id.home_icon)
        val favoriteIcon = menuLayout.findViewById<ImageView>(R.id.favorites_icon)


        homeIcon.setImageResource(R.drawable.home_2)
        getCategories()
        setupSection("section_1",binding.section1MainLayout,binding.section1Title,binding.section1RecyclerView)
        setupSection("section_2",binding.section2MainLayout,binding.section2Title,binding.section2RecyclerView)
        setupSection("section_3",binding.section3MainLayout,binding.section3Title,binding.section3RecyclerView)
        setupMostlyPlayed("mostly_played",binding.mostlyPlayedMainLayout,binding.mostlyPlayedTitle,binding.mostlyPlayedRecyclerView)
        setupSingerSection()

        binding.optionBtn.setOnClickListener {
            showPopupWindow()
        }

        searchIcon.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        userIcon.setOnClickListener {
            val userAccountIntent = Intent(this, UserAccountActivity::class.java)
            startActivity(userAccountIntent)
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
        binding.singerSectionMainLayout.setOnClickListener {
            // Điều hướng đến SingerActivity
            val intent = Intent(this, SingerActivity::class.java)
            startActivity(intent)
        }
    }


    fun showPopupWindow() {
        // Inflate layout from XML
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_menu_layout, null)

        // Create PopupWindow with the inflated layout
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
            popupWindow.dismiss()  // Chỉ đóng popup mà không làm gì thêm
        }
        view.findViewById<LinearLayout>(R.id.all_songs_layout).setOnClickListener {
            startActivity(Intent(this, AllSongsActivity::class.java))  // Redirect to AllSongsActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }
        view.findViewById<LinearLayout>(R.id.admin_song_layout).setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))  // Redirect to AllSongsActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }
        view.findViewById<LinearLayout>(R.id.playlist_layout).setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))  // Redirect to AllSongsActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }
        view.findViewById<LinearLayout>(R.id.singer_layout).setOnClickListener {
            startActivity(Intent(this, SingerActivity::class.java))  // Redirect to AllSongsActivity
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


    //Categories
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }


    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }



    //Section
    fun setupSection(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                    }
                }
            }
    }

    fun setupMostlyPlayed(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                //get most played songs
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count",Query.Direction.DESCENDING)
                    .limit(5)
                    .get().addOnSuccessListener {songListSnapshot->
                        val songModelList = songListSnapshot.toObjects<SongModel>()
                        val songsIdList = songModelList.map { it.id }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                            }
                        }
                    }
            }
    }
    fun setupSingerSection() {
        FirebaseFirestore.getInstance().collection("songs")
            .get()
            .addOnSuccessListener { songListSnapshot ->
                val singerList = mutableListOf<SingerModel>()

                // Lấy thông tin ca sĩ từ trường `subtitle` của bài hát
                songListSnapshot.toObjects(SongModel::class.java).forEach { song ->
                    val singer = SingerModel(
                        name = song.subtitle, // Thay đổi từ `detailSinger` thành `subtitle`
                        imageUrl = song.singerUrl
                    )
                    singerList.add(singer)
                }

                // Loại bỏ các ca sĩ trùng lặp dựa trên tên
                val uniqueSingers = singerList.distinctBy { it.name }

                // Kiểm tra danh sách ca sĩ sau khi loại bỏ trùng lặp
                Log.d("SingerList", "Unique Singer List: $uniqueSingers")

                // Setup RecyclerView với danh sách ca sĩ đã loại bỏ trùng lặp
                if (uniqueSingers.isNotEmpty()) {
                    setupSingerRecyclerView(uniqueSingers)
                    binding.singerSectionMainLayout.visibility = View.VISIBLE
                } else {
                    Log.d("SingerSection", "No singers available.")
                    binding.singerSectionMainLayout.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to fetch singers: ${e.message}")
            }
    }

    // Đảm bảo ID RecyclerView chính xác
    fun setupSingerRecyclerView(singerList: List<SingerModel>) {
        val singerAdapter = SingerMainAdapter(singerList, this)
        binding.singerSectionRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.singerSectionRecyclerView.adapter = singerAdapter

        // Hiển thị phần này khi có dữ liệu
        binding.singerSectionMainLayout.visibility = View.VISIBLE
    }


}