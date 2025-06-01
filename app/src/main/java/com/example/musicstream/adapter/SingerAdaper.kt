package com.example.musicstream.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicstream.DetailSingerActivity
import com.example.musicstream.databinding.ItemSingerBinding
import com.example.musicstream.models.SingerModel

class SingerAdapter(
    private val singerList: List<SingerModel>,
    private val context: Context
) : RecyclerView.Adapter<SingerAdapter.SingerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerViewHolder {
        val binding = ItemSingerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingerViewHolder, position: Int) {
        val singer = singerList[position]
        holder.bind(singer)
    }

    override fun getItemCount(): Int = singerList.size

    inner class SingerViewHolder(private val binding: ItemSingerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(singer: SingerModel) {
            binding.singerName.text = singer.name

            Glide.with(context)
                .load(singer.imageUrl)
                .circleCrop() // Cắt hình ảnh thành hình tròn
                .into(binding.singerImage)

            // Xử lý sự kiện khi nhấn vào ca sĩ
            binding.root.setOnClickListener {
                val intent = Intent(context, DetailSingerActivity::class.java).apply {
                    putExtra("singerName", singer.name)
                    putExtra("singerImageUrl", singer.imageUrl)
                    putExtra("singerDetail", singer.name) // Cập nhật nếu có dữ liệu chi tiết
                }
                context.startActivity(intent)
            }
        }
    }
}
