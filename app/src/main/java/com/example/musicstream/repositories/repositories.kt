package com.example.musicstream.repositories

import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SongRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val songsCollection = firestore.collection("songs")

    // Function to search songs by title or artist from Firestore
    suspend fun searchSongs(query: String): List<SongModel> {
        return try {
            // Query the Firestore database to find matching songs by title or artist
            val snapshot = songsCollection
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .await() // Suspend function to wait for the result

            // Convert the snapshot to a list of SongModel
            snapshot.toObjects(SongModel::class.java)
        } catch (e: Exception) {
            // Handle any errors that occur
            emptyList() // Return an empty list in case of error
        }
    }
}
