package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.models.PlaylistModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AddPlaylistActivity : AppCompatActivity() {

    private lateinit var playlistNameEditText: EditText
    private lateinit var playlistDescriptionEditText: EditText
    private lateinit var createPlaylistButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_playlist)

        val exitButton = findViewById<View>(R.id.exit_button)
        exitButton.setOnClickListener {
            finish() // Close the activity when exit button is pressed
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        playlistNameEditText = findViewById(R.id.playlistNameEditText)
        playlistDescriptionEditText = findViewById(R.id.playlistDescriptionEditText)
        createPlaylistButton = findViewById(R.id.createPlaylistButton)

        createPlaylistButton.setOnClickListener {
            createPlaylist()
        }
    }

    private fun createPlaylist() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "You need to log in to create a playlist.", Toast.LENGTH_SHORT).show()
            return
        }

        val playlistName = playlistNameEditText.text.toString().trim()
        val playlistDescription = playlistDescriptionEditText.text.toString().trim()

        if (playlistName.isEmpty()) {
            Toast.makeText(this, "Please enter a playlist name.", Toast.LENGTH_SHORT).show()
            return
        }

        val playlistId = UUID.randomUUID().toString()
        val playlist = PlaylistModel(
            id = playlistId,
            name = playlistName,
            description = playlistDescription,
            userUid = uid
        )

        db.collection("playlists").document(playlistId).set(playlist)
            .addOnSuccessListener {
                val intent = Intent()
                intent.putExtra("newPlaylist", playlist)
                setResult(RESULT_OK, intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create playlist: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
