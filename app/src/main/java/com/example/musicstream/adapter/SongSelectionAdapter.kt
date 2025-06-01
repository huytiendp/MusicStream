package com.example.musicstream.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.R
import com.example.musicstream.models.SongModel

class SongSelectionAdapter(
    private val songs: List<SongModel>,
    private val selectedSongs: MutableList<String>
) : RecyclerView.Adapter<SongSelectionAdapter.SongViewHolder>() {

    // Callback để báo khi danh sách bài hát thay đổi
    private var onSongSelectionChanged: ((List<String>) -> Unit)? = null

    fun setOnSongSelectionChanged(callback: (List<String>) -> Unit) {
        onSongSelectionChanged = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_selection, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)

        // Xử lý checkbox
        holder.checkBox.setOnCheckedChangeListener(null) // Reset listener trước khi cập nhật
        holder.checkBox.isChecked = selectedSongs.contains(song.id) // Giữ nguyên trạng thái checkbox
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!selectedSongs.contains(song.id)) {
                    selectedSongs.add(song.id)
                }
            } else {
                if (selectedSongs.contains(song.id)) {
                    selectedSongs.remove(song.id)
                }
            }
            // Gọi callback để báo khi danh sách thay đổi
            onSongSelectionChanged?.invoke(selectedSongs)
        }
    }

    override fun getItemCount() = songs.size

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.songTitleTextView)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.songSubtitleTextView)
        private val coverImageView: ImageView = itemView.findViewById(R.id.songCoverImageView)
        val checkBox: CheckBox = itemView.findViewById(R.id.songCheckBox)

        fun bind(song: SongModel) {
            titleTextView.text = song.title
            subtitleTextView.text = song.subtitle

            // Chỉ sử dụng Glide mà không có hình ảnh chờ và hình ảnh lỗi
            Glide.with(itemView.context)
                .load(song.coverUrl)  // Tải hình ảnh từ URL
                .into(coverImageView)  // Chèn vào ImageView
        }
    }
}

