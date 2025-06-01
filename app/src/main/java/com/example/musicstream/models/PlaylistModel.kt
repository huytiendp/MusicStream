package com.example.musicstream.models

import java.io.Serializable

data class PlaylistModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val songs: List<String> = emptyList(), // Mảng các ID bài hát
    val userUid: String = "" // UID của người dùng
): Serializable
