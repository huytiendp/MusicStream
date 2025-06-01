package com.example.musicstream

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.adapter.ListeningHistoryAdapter
import com.example.musicstream.models.ListeningHistoryModel
import com.example.musicstream.models.SongModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListeningHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListeningHistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_listening_history)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        recyclerView = findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListeningHistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        val exitButton = findViewById<View>(R.id.exit_button)
        exitButton.setOnClickListener {
            finish() // Đóng Activity khi nhấn nút
        }

        fetchListeningHistory()
    }

    private fun fetchListeningHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usage_history")
            .whereEqualTo("userUid", userId)
            .get()
            .addOnSuccessListener { result ->
                val historyList = mutableListOf<ListeningHistoryModel>()
                val songMap = mutableMapOf<String, SongModel>()

                for (document in result) {
                    val model = document.toObject(ListeningHistoryModel::class.java)
                    historyList.add(model)
                }

                if (historyList.isNotEmpty()) {
                    // Sắp xếp lịch sử theo timestamp giảm dần
                    historyList.sortByDescending { it.timestamp?.toDate() }

                    val songIds = historyList.map { it.songId }.toSet()

                    db.collection("songs") // Assumes your song collection is named "songs"
                        .whereIn("id", songIds.toList())
                        .get()
                        .addOnSuccessListener { songResult ->
                            for (songDoc in songResult) {
                                val song = songDoc.toObject(SongModel::class.java)
                                songMap[song.id] = song
                            }

                            val fullHistory = historyList.map { history ->
                                val song = songMap[history.songId]
                                Pair(history, song)
                            }

                            // Truyền danh sách đã sắp xếp vào adapter
                            adapter.submitList(fullHistory)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error fetching songs: ", e)
                            Toast.makeText(this, "Không thể tải dữ liệu bài hát!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không thể lấy dữ liệu!", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error fetching usage history: ", e)
            }
    }


}
