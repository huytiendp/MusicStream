package com.example.musicstream.models

import com.google.firebase.Timestamp

data class ListeningHistoryModel(
    val userUid: String = "",
    val songId: String = "",
    val timestamp: Timestamp? = null
)
