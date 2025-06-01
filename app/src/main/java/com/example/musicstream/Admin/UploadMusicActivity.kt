package com.example.musicstream.Admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.R
import com.google.firebase.firestore.FirebaseFirestore

class UploadMusicActivity : AppCompatActivity() {

    private lateinit var btnSelectMusic: Button
    private lateinit var btnSelectImage: Button
    private lateinit var btnUpload: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var etTitle: EditText
//    private lateinit var etDocumentId: EditText
    private lateinit var etSubtitle: EditText
    private lateinit var etLyrics: EditText // New EditText for lyrics
    private lateinit var etDetailSinger: EditText
    private lateinit var btnSelectArtistImage: Button
    private lateinit var songsStorage: SongsStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var ivSelectedImage: ImageView


    private val PICK_MUSIC_REQUEST = 1
    private val PICK_IMAGE_REQUEST = 2
    private var musicUri: Uri? = null
    private var imageUri: Uri? = null
    private var artistImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_music)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút quay lại trên Toolbar

        ivSelectedImage = findViewById(R.id.ivSelectedImage)

        // Xử lý sự kiện khi nhấn nút quay lại
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        etTitle = findViewById(R.id.etTitle)
//        etDocumentId = findViewById(R.id.etDocumentId)
        etSubtitle = findViewById(R.id.etSubtitle)
        etLyrics = findViewById(R.id.etLyrics) // Mapping lyrics EditText
        etDetailSinger = findViewById(R.id.etDetailSinger)
        btnSelectMusic = findViewById(R.id.btnSelectMusic)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSelectArtistImage = findViewById(R.id.btnSelectArtistImage)
        btnUpload = findViewById(R.id.btnUpload)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)

        songsStorage = SongsStorage()
        firestore = FirebaseFirestore.getInstance()

        btnSelectMusic.setOnClickListener {
            openFileChooser(PICK_MUSIC_REQUEST)
        }

        btnSelectImage.setOnClickListener {
            openFileChooser(PICK_IMAGE_REQUEST)
        }

        btnSelectArtistImage.setOnClickListener {
            openFileChooser(PICK_IMAGE_REQUEST, true)
        }

        btnUpload.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val subtitle = etSubtitle.text.toString().trim()
            val lyrics = etLyrics.text.toString().trim()
            val detailSinger = etDetailSinger.text.toString().trim()

            if (title.isEmpty()) {
                tvStatus.text = "Vui lòng nhập tên bài hát."
            } else if (subtitle.isEmpty()) {
                tvStatus.text = "Vui lòng nhập tên ca sĩ."
            } else if (musicUri != null && imageUri != null && artistImageUri != null) {
                // Lấy ID bài hát tiếp theo tự động
                getNextSongId({ customDocumentId ->
                    uploadMusicAndImageToFirebase(title, subtitle, lyrics, detailSinger, customDocumentId)
                }, { exception ->
                    tvStatus.text = "Không thể tạo ID bài hát: ${exception.message}"
                })
            } else {
                tvStatus.text = "Vui lòng chọn nhạc, ảnh bài hát và ảnh ca sĩ trước khi tải lên."
            }
        }

    }

    private fun openFileChooser(requestCode: Int, isArtistImage: Boolean = false) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = if (requestCode == PICK_MUSIC_REQUEST) "audio/*" else "image/*"
        startActivityForResult(intent, requestCode)

        if (isArtistImage) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST + 10)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                PICK_MUSIC_REQUEST -> {
                    musicUri = data.data
                    tvStatus.text = "Đã chọn tệp nhạc."
                }
                PICK_IMAGE_REQUEST -> {
                    imageUri = data.data
                    tvStatus.text = "Đã chọn tệp ảnh bài hát."
                    ivSelectedImage.visibility = View.VISIBLE
                    ivSelectedImage.setImageURI(imageUri)
                }
                PICK_IMAGE_REQUEST + 10 -> {
                    artistImageUri = data.data
                    tvStatus.text = "Đã chọn tệp ảnh ca sĩ."
                }
            }
        }
    }
    private fun getNextSongId(callback: (String) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("songs")
            .get()
            .addOnSuccessListener { result ->
                val songIds = result.documents.mapNotNull { doc ->
                    doc.id.substringAfter("song_").toIntOrNull()
                }.sorted()
                val nextId = if (songIds.isNotEmpty()) songIds.last() + 1 else 1
                callback("song_$nextId")
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    private fun uploadMusicAndImageToFirebase(title: String, subtitle: String, lyrics: String, detailSinger: String, customDocumentId: String) {
        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Đang tải lên nhạc và hình ảnh..."

        musicUri?.let { musicUri ->
            songsStorage.uploadMusic(musicUri, { musicDownloadUrl ->
                Log.d("UploadMusicActivity", "Music Download URL: $musicDownloadUrl")

                imageUri?.let { imageUri ->
                    songsStorage.uploadImage(imageUri, { imageDownloadUrl ->
                        Log.d("UploadMusicActivity", "Image Download URL: $imageDownloadUrl")

                        artistImageUri?.let { artistImageUri ->
                            songsStorage.uploadArtistImage(artistImageUri, { artistImageDownloadUrl ->
                                Log.d("UploadMusicActivity", "Artist Image Download URL: $artistImageDownloadUrl")

                                // Lưu tất cả thông tin vào Firestore
                                saveMusicInfoToFirestore(musicDownloadUrl, imageDownloadUrl, artistImageDownloadUrl, title, subtitle, lyrics, detailSinger, customDocumentId)
                                progressBar.visibility = View.GONE

                            }, { exception ->
                                progressBar.visibility = View.GONE
                                tvStatus.text = "Tải lên ảnh ca sĩ thất bại: ${exception.message}"
                                Log.e("UploadMusicActivity", "Artist image upload failed", exception)
                            })
                        }

                    }, { exception ->
                        progressBar.visibility = View.GONE
                        tvStatus.text = "Tải lên hình ảnh thất bại: ${exception.message}"
                        Log.e("UploadMusicActivity", "Image upload failed", exception)
                    })
                }

            }, { exception ->
                progressBar.visibility = View.GONE
                tvStatus.text = "Tải lên nhạc thất bại: ${exception.message}"
                Log.e("UploadMusicActivity", "Music upload failed", exception)
            })
        }
    }

    private fun saveMusicInfoToFirestore(
        musicDownloadUrl: String,
        imageDownloadUrl: String,
        artistImageDownloadUrl: String,
        title: String,
        subtitle: String,
        lyrics: String,
        detailSinger: String,
        customDocumentId: String
    ) {
        val musicData = hashMapOf(
            "id" to customDocumentId,
            "url" to musicDownloadUrl,
            "coverUrl" to imageDownloadUrl,
            "singerUrl" to artistImageDownloadUrl,
            "title" to title,
            "subtitle" to subtitle,
            "lyric" to lyrics,
            "detailSinger" to detailSinger  // Thêm detailSinger vào hashMap
        )

        firestore.collection("songs")
            .document(customDocumentId)
            .set(musicData)
            .addOnSuccessListener {
                Log.d("UploadMusicActivity", "DocumentSnapshot successfully written with ID: $customDocumentId")
                tvStatus.text = "Đã lưu vào Firestore thành công!"
            }
            .addOnFailureListener { e ->
                Log.w("UploadMusicActivity", "Error adding document", e)
                tvStatus.text = "Lưu vào Firestore thất bại: ${e.message}"
            }
    }
}
