package com.example.musicstream

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicstream.models.SingerModel
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

object MyExoplayer {

    private var exoPlayer: ExoPlayer? = null
    private var currentSong: SongModel? = null
    private var songList: List<SongModel> = emptyList() // Danh sách bài hát
    private var currentSongIndex: Int = 0 // Chỉ số bài hát hiện tại

    fun getCurrentSong(): SongModel? {
        return currentSong
    }

    fun getInstance(): ExoPlayer? {
        if (exoPlayer == null) {
            Log.e("MyExoplayer", "ExoPlayer chưa được khởi tạo. Hãy gọi startPlaying trước.")
        }
        return exoPlayer
    }


    fun setSongList(songs: List<SongModel>) {
        songList = songs
        val mediaItems = songs.map { song -> MediaItem.fromUri(song.url) }
        exoPlayer?.setMediaItems(mediaItems) // Thêm toàn bộ danh sách bài hát
        exoPlayer?.prepare() // Chuẩn bị phát
    }

    fun startPlaying(context: Context, song: SongModel) {
        if (exoPlayer == null)
            exoPlayer = ExoPlayer.Builder(context).build()

        if (currentSong != song) {
            currentSong = song
            updateCount()
            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()
            }
        }
    }

    private fun updateCount() {
        currentSong?.id?.let { id ->
            FirebaseFirestore.getInstance().collection("songs")
                .document(id)
                .get().addOnSuccessListener {
                    var latestCount = it.getLong("count")
                    latestCount = latestCount?.plus(1) ?: 1L

                    FirebaseFirestore.getInstance().collection("songs")
                        .document(id)
                        .update(mapOf("count" to latestCount))
                }
        }
    }

    fun loadSongById(context: Context, songId: String, onComplete: (SongModel?) -> Unit) {
        FirebaseFirestore.getInstance().collection("songs")
            .document(songId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val song = document.toObject(SongModel::class.java)?.apply {
                        id = document.id // Đảm bảo song có ID
                    }
                    onComplete(song)
                } else {
                    onComplete(null) // Không tìm thấy bài hát
                }
            }
            .addOnFailureListener {
                onComplete(null) // Xử lý lỗi
            }
    }
    fun extractUniqueSingers(songList: List<SongModel>): List<SingerModel> {
        val singerMap = mutableMapOf<String, String>() // Map để tránh lặp ca sĩ

        for (song in songList) {
            singerMap[song.subtitle] = song.singerUrl
        }

        return singerMap.map { SingerModel(it.key, it.value) }
    }



}
