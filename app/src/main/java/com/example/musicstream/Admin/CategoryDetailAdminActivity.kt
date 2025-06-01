package com.example.musicstream.Admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.R
import com.example.musicstream.adapter.SongSelectionAdapter
import com.example.musicstream.databinding.ActivityCategoryDetailAdminBinding
import com.example.musicstream.models.CategoryModel
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class CategoryDetailAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryDetailAdminBinding
    private lateinit var category: CategoryModel
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Setup Toolbar with Back Button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Category Details"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Return to the previous screen
        }

        // Get CategoryModel from intent
        category = intent.getSerializableExtra("category") as CategoryModel
        populateCategoryDetails(category)
    }

    // Inflate the menu to add items to the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_category_admin_menu, menu) // Inflate the new menu
        return true
    }

    // Handle item selection in the toolbar menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_song -> {
                showAddSongDialog()
                true
            }
            R.id.action_edit -> {
                val intent = Intent(this, EditCategoryAdminActivity::class.java)
                intent.putExtra("category", category)
                startActivity(intent)
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun populateCategoryDetails(category: CategoryModel) {
        binding.nameTextView.text = category.name
        Glide.with(this).load(category.coverUrl).into(binding.coverImageView)

        // Lấy chi tiết bài hát từ Firestore
        db.collection("songs").whereIn("id", category.songs).get()
            .addOnSuccessListener { snapshot ->
                val songs = snapshot.toObjects(SongModel::class.java)
                setupRecyclerView(songs)
            }
            .addOnFailureListener {
                // Xử lý lỗi nếu cần
            }
    }

    private fun setupRecyclerView(songs: List<SongModel>) {
        val adapter = SongListAdminAdapter(songs)
        binding.songRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songRecyclerView.adapter = adapter
    }

    private fun showAddSongDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_song, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.songRecyclerView)

        val selectedSongs = mutableListOf<String>() // Danh sách ID bài hát đã chọn

        // Khởi tạo adapter và truyền vào callback
        val songs = mutableListOf<SongModel>()
        val adapter = SongSelectionAdapter(songs, selectedSongs)

        // Cập nhật Firestore khi danh sách bài hát thay đổi
        adapter.setOnSongSelectionChanged { updatedSelectedSongs ->
            category.songs = updatedSelectedSongs
            updateCategoryInFirestore()
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Tải danh sách bài hát từ Firestore
        db.collection("songs").get().addOnSuccessListener { snapshot ->
            val fetchedSongs = snapshot.toObjects(SongModel::class.java)
            songs.clear()
            songs.addAll(fetchedSongs)

            // Đánh dấu các bài hát đã có trong danh mục
            category.songs.forEach { songId ->
                val song = fetchedSongs.find { it.id == songId }
                song?.let {
                    selectedSongs.add(it.id)  // Thêm ID bài hát vào danh sách đã chọn
                }
            }

            // Cập nhật lại danh sách và adapter
            adapter.notifyDataSetChanged() // Cập nhật danh sách bài hát
        }

        // Hiển thị hộp thoại
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Chọn Bài Hát")
            .setPositiveButton("Thêm") { dialog, _ ->
                if (selectedSongs.isNotEmpty()) {
                    addSongsToCategory(selectedSongs) // Thêm bài hát đã chọn vào danh mục
                }
                dialog.dismiss() // Đóng hộp thoại ngay lập tức
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun addSongsToCategory(songIds: List<String>) {
        val newSongs = category.songs + songIds
        category.songs = newSongs.distinct() // Loại bỏ bài hát trùng lặp nếu có

        // Lấy danh sách bài hát từ Firestore và cập nhật giao diện
        db.collection("songs").whereIn("id", category.songs).get()
            .addOnSuccessListener { snapshot ->
                // Lấy danh sách bài hát từ snapshot
                val songs = snapshot.toObjects(SongModel::class.java)

                // Cập nhật RecyclerView
                updateRecyclerView(songs)
            }
            .addOnFailureListener {
                // Xử lý lỗi (nếu cần)
            }

        updateCategoryInFirestore() // Cập nhật danh mục trong Firestore
    }

    private fun updateRecyclerView(songs: List<SongModel>) {
        val adapter = SongListAdminAdapter(songs)
        binding.songRecyclerView.adapter = adapter
    }

    private fun updateCategoryInFirestore() {
        val categoryId = category.name
        db.collection("category").document(categoryId)
            .update("songs", category.songs)
            .addOnSuccessListener {
                // Cập nhật thành công
            }
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cần
            }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác Nhận Xóa")
            .setMessage("Bạn có chắc chắn muốn xóa danh mục này không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                deleteCategory()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun deleteCategory() {
        val categoryId = category.name
        db.collection("category").document(categoryId)
            .delete()
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cần
            }
    }
}

