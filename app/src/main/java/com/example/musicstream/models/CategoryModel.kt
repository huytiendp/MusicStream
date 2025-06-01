package com.example.musicstream.models

import java.io.Serializable

data class CategoryModel(
    val name: String,
    val coverUrl: String,
    var songs: List<String>
) : Serializable {
    constructor() : this("", "", listOf())
}
