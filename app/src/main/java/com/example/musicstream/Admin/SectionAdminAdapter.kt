package com.example.musicstream.Admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.databinding.SectionAdminRecyclerRowBinding
import com.example.musicstream.models.SectionModel
import com.google.firebase.firestore.FirebaseFirestore

class SectionAdminAdapter(private val sectionIdList: List<String>) :
    RecyclerView.Adapter<SectionAdminAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SectionAdminRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data and set up click listener to open SectionDetailAdminActivity
        fun bindData(sectionId: String) {
            FirebaseFirestore.getInstance().collection("sections")
                .document(sectionId).get()
                .addOnSuccessListener { document ->
                    val section = document.toObject(SectionModel::class.java)
                    section?.let {
                        binding.sectionTitleTextView.text = it.name
                        binding.sectionSongCountTextView.text = "${it.songs.size} songs"
                        Glide.with(binding.sectionImageView)
                            .load(it.coverUrl)
                            .apply(RequestOptions().transform(RoundedCorners(32)))
                            .into(binding.sectionImageView)

                        // Set click listener to open SectionDetailAdminActivity
                        binding.root.setOnClickListener { view ->
                            val intent = Intent(view.context, SectionDetailAdminActivity::class.java)
                            intent.putExtra("sectionId", sectionId) // Pass section ID
                            view.context.startActivity(intent)
                        }
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SectionAdminRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = sectionIdList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(sectionIdList[position])
    }
}
