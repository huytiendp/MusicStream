package com.example.musicstream.models

data class SongModel(
    var id: String,
    val title: String,
    val subtitle: String,
    val url: String,
    val coverUrl: String,
    var lyric: String,
    var singerUrl: String,
    var detailSinger : String,
    val count: Long = 0,

    ){
    constructor() : this("","","","","","","","")
}
