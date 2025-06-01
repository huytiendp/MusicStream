package com.example.musicstream.Admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.databinding.SongItemRecyclerRowBinding
import com.example.musicstream.models.SongModel

class SongAdminAdapter(private var songList: List<SongModel>) :
    RecyclerView.Adapter<SongAdminAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SongItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(song: SongModel) {
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
            binding.songCountTextView.text = song.count.toString()  // Convert count to String
            Glide.with(binding.songCoverImageView.context)
                .load(song.coverUrl)
                .into(binding.songCoverImageView)

            // Set click listener to open SongDetailAdminActivity
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, SongDetailAdminActivity::class.java)
                intent.putExtra("songId", song.id)  // Pass song ID to detail activity
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SongItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = songList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songList[position])
    }

    // Add updateData method to update song list
    fun updateData(newSongList: List<SongModel>) {
        songList = newSongList
        notifyDataSetChanged()
    }
}

