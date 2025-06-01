package com.example.musicstream.Admin

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SongsStorage {

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference.child("songs")
    private val imageStorageRef: StorageReference = storage.reference.child("songs_image") // Thư mục ảnh bài hát
    private val artistImageStorageRef: StorageReference = storage.reference.child("singers_image") // Thư mục ảnh nghệ sĩ

    fun uploadMusic(
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileName = "song_${System.currentTimeMillis()}.mp3"
        val musicRef = storageRef.child(fileName)

        val uploadTask = musicRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            musicRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
                Log.d("SongsStorage", "Music upload successful! Download URL: $downloadUrl")
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
            Log.e("SongsStorage", "Music upload failed", exception)
        }
    }

    fun uploadImage(
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val imageRef = imageStorageRef.child(fileName)

        val uploadTask = imageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
                Log.d("SongsStorage", "Image upload successful! Download URL: $downloadUrl")
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
            Log.e("SongsStorage", "Image upload failed", exception)
        }
    }

    fun uploadArtistImage(
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileName = "artist_image_${System.currentTimeMillis()}.jpg"
        val artistImageRef = artistImageStorageRef.child(fileName)

        val uploadTask = artistImageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            artistImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
                Log.d("SongsStorage", "Artist image upload successful! Download URL: $downloadUrl")
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
            Log.e("SongsStorage", "Artist image upload failed", exception)
        }
    }
}
