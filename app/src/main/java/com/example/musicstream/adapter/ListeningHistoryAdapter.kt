package com.example.musicstream.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.R
import com.example.musicstream.models.ListeningHistoryModel
import com.example.musicstream.models.SongModel
import java.text.SimpleDateFormat
import java.util.Locale

class ListeningHistoryAdapter(
    private var historyList: List<Pair<ListeningHistoryModel, SongModel?>>
) : RecyclerView.Adapter<ListeningHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songImageView: ImageView = view.findViewById(R.id.songImageView)
        val songTextView: TextView = view.findViewById(R.id.songTextView)
        val singerTextView: TextView = view.findViewById(R.id.singerTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val (history, song) = historyList[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Hiển thị hình ảnh bài hát
        if (song?.coverUrl != null) {
            Glide.with(holder.itemView.context)
                .load(song.coverUrl)
                .placeholder(R.drawable.feeling) // Ảnh placeholder
                .into(holder.songImageView)
        } else {
            holder.songImageView.setImageResource(R.drawable.feeling)
        }

        // Hiển thị tên bài hát
        holder.songTextView.text = song?.title ?: "Unknown Song"

        // Hiển thị tên ca sĩ
        // Hiển thị tên ca sĩ từ subtitle thay vì detailSinger
        holder.singerTextView.text = song?.subtitle ?: "Unknown Artist"

        // Hiển thị thời gian nghe
        history.timestamp?.let { timestamp ->
            holder.dateTextView.text = dateFormat.format(timestamp.toDate())
        } ?: run {
            holder.dateTextView.text = "Unknown time"
        }
    }

    override fun getItemCount(): Int = historyList.size

    fun submitList(newList: List<Pair<ListeningHistoryModel, SongModel?>>) {
        historyList = newList
        notifyDataSetChanged()
    }
}


