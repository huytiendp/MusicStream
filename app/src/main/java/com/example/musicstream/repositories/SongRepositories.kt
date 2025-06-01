package com.example.musicstream.repositories

import com.example.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SongRepositories {

    private val firestore = FirebaseFirestore.getInstance()
    private val songsCollection = firestore.collection("songs")

    // Function to get all songs from Firestore
    suspend fun getAllSongs(): List<SongModel> {
        return try {
            // Fetch all songs from the Firestore collection
            val snapshot = songsCollection.get().await()
            // Convert the Firestore snapshot to a list of SongModel
            snapshot.toObjects(SongModel::class.java)
        } catch (e: Exception) {
            // Handle any errors by returning an empty list
            emptyList()
        }
    }

    // Function to search songs by title or artist from Firestore
    suspend fun searchSongs(query: String): List<SongModel> {
        return try {
            // Query the Firestore database to find matching songs by title or artist
            val titleSnapshot = songsCollection
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .await()

            // Query for matching artist names
            val artistSnapshot = songsCollection
                .whereGreaterThanOrEqualTo("artist", query)
                .whereLessThanOrEqualTo("artist", query + "\uf8ff")
                .get()
                .await()

            // Combine the results and remove duplicates if any
            val titleSongs = titleSnapshot.toObjects(SongModel::class.java)
            val artistSongs = artistSnapshot.toObjects(SongModel::class.java)

            // Merge both lists and remove duplicates by creating a set
            (titleSongs + artistSongs).distinctBy { it.id }
        } catch (e: Exception) {
            // Handle any errors by returning an empty list
            emptyList()
        }
    }
}
