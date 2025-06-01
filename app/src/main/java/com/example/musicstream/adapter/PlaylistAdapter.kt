package com.example.musicstream.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.R
import com.example.musicstream.SongPlaylistActivity
import com.example.musicstream.models.PlaylistModel

class PlaylistAdapter(
    private val playlists: List<PlaylistModel>,
    private val context: Context,
    private val isSelectionMode: Boolean = false
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private val selectedPlaylists = mutableSetOf<PlaylistModel>()

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.playlistName)
        val description: TextView = view.findViewById(R.id.playlistDescription)
        val cover: ImageView = view.findViewById(R.id.playlistCover)
        val checkbox: CheckBox = view.findViewById(R.id.playlistCheckbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.name.text = playlist.name
        holder.description.text = playlist.description

        holder.checkbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.checkbox.isChecked = selectedPlaylists.contains(playlist)

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                if (selectedPlaylists.contains(playlist)) {
                    selectedPlaylists.remove(playlist)
                } else {
                    selectedPlaylists.add(playlist)
                }
                notifyItemChanged(position)
            } else {
                val intent = Intent(context, SongPlaylistActivity::class.java)
                intent.putExtra("playlistId", playlist.id)
                context.startActivity(intent)
            }
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPlaylists.add(playlist)
            } else {
                selectedPlaylists.remove(playlist)
            }
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun getSelectedPlaylists(): List<PlaylistModel> = selectedPlaylists.toList()
}

