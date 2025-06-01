package com.example.musicstream.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.MyExoplayer
import com.example.musicstream.PlayerActivity
import com.example.musicstream.databinding.SongListItemRecyclerRowBinding
import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteAdapter(
    val songIdList: MutableList<String>,
    private val onSongLongPress: (String) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(private val binding: SongListItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(songId: String, onSongLongPress: (String) -> Unit) {
            FirebaseFirestore.getInstance().collection("songs")
                .document(songId).get()
                .addOnSuccessListener { document ->
                    val song = document.toObject(SongModel::class.java)
                    song?.let {
                        binding.songTitleTextView.text = it.title
                        binding.songSubtitleTextView.text = it.subtitle

                        Glide.with(binding.songCoverImageView.context)
                            .load(it.coverUrl)
                            .apply(RequestOptions().transform(RoundedCorners(16)))
                            .into(binding.songCoverImageView)

                        // Xử lý sự kiện click bình thường
                        binding.root.setOnClickListener { view ->
                            MyExoplayer.startPlaying(view.context, it)
                            view.context.startActivity(
                                Intent(view.context, PlayerActivity::class.java)
                            )
                        }

                        // Xử lý sự kiện nhấn giữ
                        binding.root.setOnLongClickListener {
                            onSongLongPress(songId)
                            true
                        }
                    }
                }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = SongListItemRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bindData(songIdList[position], onSongLongPress)
    }

    override fun getItemCount(): Int = songIdList.size
}

