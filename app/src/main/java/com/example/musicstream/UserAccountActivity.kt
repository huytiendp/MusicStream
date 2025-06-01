package com.example.musicstream

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicstream.databinding.ActivityUserAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")

        // Load user data
        loadUserData()

        // Set up navigation icons
        setupNavigation()

        // Handle avatar image click
        binding.avatarImageView.setOnClickListener {
            selectImageFromGallery()
        }
    }

    private fun setupNavigation() {
        val homeIcon = binding.root.findViewById<ImageView>(R.id.home_icon)
        val searchIcon = binding.root.findViewById<ImageView>(R.id.search_icon)
        val favoriteIcon = binding.root.findViewById<ImageView>(R.id.favorites_icon)
        val userIcon = binding.root.findViewById<ImageView>(R.id.user_icon)

        userIcon.setImageResource(R.drawable.person_2)

        homeIcon.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        favoriteIcon.setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java))
        }

        binding.root.findViewById<LinearLayout>(R.id.playlistOption).setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }

        binding.root.findViewById<LinearLayout>(R.id.listeningHistoryOption).setOnClickListener {
            startActivity(Intent(this, ListeningHistoryActivity::class.java))
        }

        binding.root.findViewById<LinearLayout>(R.id.settingsOption).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.root.findViewById<LinearLayout>(R.id.change_password_option).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    private fun loadUserData() {
        val userId = currentUser.uid
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.emailTextView.text = currentUser.email
                    val avatarUrl = document.getString("avatarUrl")
                    if (avatarUrl != null) {
                        Glide.with(this).load(avatarUrl).into(binding.avatarImageView)
                    }
                } else {
                    createDefaultUserDocument(userId)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDefaultUserDocument(userId: String) {
        val userData = mapOf(
            "email" to currentUser.email,
            "avatarUrl" to null
        )
        firestore.collection("users").document(userId).set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Default user document created", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to create default document: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.avatarImageView.setImageURI(imageUri)
            uploadAvatarToFirebase()
        }
    }

    private fun uploadAvatarToFirebase() {
        imageUri?.let { uri ->
            val avatarRef = storageRef.child("avatars/${currentUser.uid}.jpg")
            avatarRef.putFile(uri)
                .addOnSuccessListener {
                    avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveAvatarUrlToFirestore(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload avatar: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveAvatarUrlToFirestore(url: String) {
        val userId = currentUser.uid
        firestore.collection("users").document(userId)
            .update("avatarUrl", url)
            .addOnSuccessListener {
                Toast.makeText(this, "Avatar updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update avatar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
