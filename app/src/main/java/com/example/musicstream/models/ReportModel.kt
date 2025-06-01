package com.example.musicstream.models

data class ReportModel(
    val songId: String = "",
    val songTitle: String = "",
    val reason: String = "",
    val reportedAt: String = "",
    val reportedBy: String = "",
    val email: String = ""
)