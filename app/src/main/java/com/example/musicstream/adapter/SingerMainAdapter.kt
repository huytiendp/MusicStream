package com.example.musicstream.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.DetailSingerActivity
import com.example.musicstream.databinding.ItemSingerMainBinding
import com.example.musicstream.models.SingerModel

class SingerMainAdapter(
    private val singerList: List<SingerModel>,
    private val context: Context
) : RecyclerView.Adapter<SingerMainAdapter.SingerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerViewHolder {
        val binding = ItemSingerMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingerViewHolder, position: Int) {
        val singer = singerList[position]
        holder.bind(singer)
    }

    override fun getItemCount(): Int = singerList.size

    inner class SingerViewHolder(private val binding: ItemSingerMainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(singer: SingerModel) {
            binding.singerMainName.text = singer.name

            // Load the image using Glide
            Glide.with(context)
                .load(singer.imageUrl)
                .circleCrop()
                .into(binding.singerMainImage)

            // Set OnClickListener for itemView
            binding.root.setOnClickListener {
                val intent = Intent(context, DetailSingerActivity::class.java).apply {
                    putExtra("singerName", singer.name)
                    putExtra("singerImageUrl", singer.imageUrl)
                    putExtra("singerDetail", " ${singer.name}")
                }
                context.startActivity(intent)
            }
        }
    }
}

