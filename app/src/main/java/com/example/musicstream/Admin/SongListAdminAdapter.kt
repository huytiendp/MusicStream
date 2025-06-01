package com.example.musicstream.Admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.R
import com.example.musicstream.models.SongModel

class SongListAdminAdapter(private var songs: List<SongModel>) :
    RecyclerView.Adapter<SongListAdminAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
        private val cover = itemView.findViewById<ImageView>(R.id.cover)

        fun bind(song: SongModel) {
            title.text = song.title
            subtitle.text = song.subtitle
            Glide.with(itemView.context).load(song.coverUrl).into(cover)

            // Handle item click event
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, SongDetailAdminActivity::class.java)
                intent.putExtra("songId", song.id)  // Passing songId to the detail activity
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    // Method to update the song list
    fun updateSongs(newSongs: List<SongModel>) {
        songs = newSongs
        notifyDataSetChanged() // Notify that the data has changed
    }
}
