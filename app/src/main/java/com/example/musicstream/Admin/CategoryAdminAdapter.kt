package com.example.musicstream.Admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.databinding.CategoryItemRecyclerRowBinding
import com.example.musicstream.models.CategoryModel

// Define a listener interface for edit button clicks
interface OnCategoryClickListener {
    fun onCategoryClick(category: CategoryModel)
    fun onEditClick(category: CategoryModel)
}

class CategoryAdminAdapter(
    private val categoryList: List<CategoryModel>,
    private val listener: OnCategoryClickListener
) : RecyclerView.Adapter<CategoryAdminAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: CategoryItemRecyclerRowBinding, private val listener: OnCategoryClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(category: CategoryModel) {
            binding.nameTextView.text = category.name
            Glide.with(binding.coverImageView).load(category.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.coverImageView)

            // Set listener for category item click
            binding.root.setOnClickListener {
                listener.onCategoryClick(category) // Notify the click on category
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CategoryItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(categoryList[position])
    }
}