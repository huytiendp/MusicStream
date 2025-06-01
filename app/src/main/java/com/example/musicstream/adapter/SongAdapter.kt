package com.example.musicstream.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.MyExoplayer
import com.example.musicstream.PlayerActivity
import com.example.musicstream.R
import com.example.musicstream.models.SongModel

class SongAdapter(private var songs: List<SongModel>, private val context: Context) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val subtitle: TextView = view.findViewById(R.id.subtitle)
        val cover: ImageView = view.findViewById(R.id.cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)

    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.subtitle.text = song.subtitle

        // Load the cover image with Glide
        Glide.with(holder.cover.context).load(song.coverUrl).apply(RequestOptions().transform(RoundedCorners(10))).into(holder.cover)

        holder.itemView.setOnClickListener {
            // Bắt đầu phát bài hát khi người dùng chọn bài hát
            MyExoplayer.startPlaying(context, song)

            // Khởi động PlayerActivity
            val intent = Intent(context, PlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun updateSongs(newSongs: List<SongModel>) {
        songs = newSongs
        notifyDataSetChanged()
    }


}
