package com.example.musicstream.models

data class SectionModel(
    val name: String = "",
    val coverUrl: String = "",
    var songs: MutableList<String> = mutableListOf() // Sử dụng MutableList
)

