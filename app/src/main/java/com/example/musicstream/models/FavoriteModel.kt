package com.example.musicstream.models

data class FavoriteModel(
    val id: String = "",
    val songs: List<String> = emptyList(),
    val userUid: String = ""
)
