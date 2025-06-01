package com.example.musicstream

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.databinding.ActivityPlayerBinding
import com.example.musicstream.models.PlaylistModel
import com.example.musicstream.models.SongModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException


class PlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayerBinding
    lateinit var exoPlayer: ExoPlayer
    private var isLyricVisible = false
    private var isTimerActive = false
    private var repeatCount = 0
    private var songList: List<SongModel> = listOf() // List to hold songs
    private var currentSongIndex: Int = 0

    var playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                playNextRandomSong()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSongs()

        intent?.data?.let { uri ->
            val songId = uri.getQueryParameter("id")
            if (!songId.isNullOrEmpty()) {
                MyExoplayer.loadSongById(this, songId) { song ->
                    if (song != null) {
                        MyExoplayer.startPlaying(this, song)
                        updateUIWithSong(song)
                        setupPlayerView()
                    } else {
                        Log.e("DeepLink", "Không tìm thấy bài hát với ID: $songId")
                    }
                }
            }
        }

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val songId = deepLink.getQueryParameter("id")
                    if (!songId.isNullOrEmpty()) {
                        MyExoplayer.loadSongById(this, songId) { song ->
                            if (song != null) {
                                MyExoplayer.startPlaying(this, song)
                                updateUIWithSong(song) // Gọi hàm cập nhật UI
                            } else {
                                Log.e("DynamicLink", "Không tìm thấy bài hát với ID: $songId")
                            }
                        }
                    }
                }
            }



        // Nút thoát ra ngoài với hiệu ứng trượt xuống
        binding.exitButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        }



        MyExoplayer.getCurrentSong()?.apply {
            // Thiết lập tiêu đề và phụ đề bài hát
            binding.songTitleTextView.text = title
            binding.songSubtitleTextView.text = subtitle
            binding.songSingerTextView.text = subtitle
            binding.singerTextView.text = subtitle
            binding.songLyricTextView.text = lyric
            binding.songDetailSingerTextView.text = detailSinger
            binding.songCountTextView.text = "$count"
            Glide.with(binding.songCoverImageView).load(coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(50))
                )
                .into(binding.songCoverImageView)

            Glide.with(binding.backgroundImageView).load(coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(50))
                )
                .into(binding.backgroundImageView)

            Glide.with(binding.singerImageView).load(singerUrl)
                .into(binding.singerImageView)

            // Thiết lập ExoPlayer
            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer
            binding.playerView.showController()
            exoPlayer.addListener(playerListener)
            recordListeningHistory()
        }
        // Xử lý khi người dùng click vào header để hiển thị/ẩn lyrics
        binding.lyricHeader.setOnClickListener {
            isLyricVisible = !isLyricVisible // Đảo ngược trạng thái hiển thị lyrics

            // Xử lý xoay icon xuống khi hiển thị và ngược lại khi ẩn lyrics
            if (isLyricVisible) {
                binding.songLyricTextView.visibility = View.VISIBLE // Hiển thị lyrics
                binding.songLyricTextView.alpha = 0f // Đặt alpha ban đầu
                binding.songLyricTextView.translationY = -50f // Đặt vị trí ban đầu bên trên
                binding.songLyricTextView.animate()
                    .alpha(1f) // Chuyển đổi alpha từ 0 đến 1
                    .translationY(0f) // Đưa nó về vị trí ban đầu
                    .setDuration(300) // Thời gian cho hiệu ứng
                    .start()

                // Xoay icon 180 độ
                binding.lyricHeader.findViewById<ImageView>(R.id.down_lyric).animate()
                    .rotation(180f) // Xoay 180 độ
                    .setDuration(300)
                    .start()
            } else {
                binding.songLyricTextView.animate()
                    .alpha(0f) // Chuyển đổi alpha từ 1 đến 0
                    .translationY(-50f) // Đưa nó lên trên một chút
                    .setDuration(300) // Thời gian cho hiệu ứng
                    .withEndAction {
                        binding.songLyricTextView.visibility = View.GONE // Ẩn lyrics sau khi hoạt ảnh hoàn thành
                    }
                    .start()

                // Xoay icon trở lại vị trí ban đầu
                binding.lyricHeader.findViewById<ImageView>(R.id.down_lyric).animate()
                    .rotation(0f) // Trở lại vị trí ban đầu (0 độ)
                    .setDuration(300)
                    .start()
            }
        }
        binding.timerIcon.setOnClickListener {
            showTimerBottomSheet() // Gọi hàm để hiển thị BottomSheetDialog
        }
        binding.repeatIcon.setOnClickListener {
            repeatCount++
            if (repeatCount > 3) { // Giả sử tối đa cho phép lặp lại 3 lần
                repeatCount = 1 // Reset về 1 nếu vượt quá 3
            }

            when (repeatCount) {
                1 -> {
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE // Chỉ lặp lại một bài
                    binding.repeatIcon.setImageResource(R.drawable.repeat_2)
                    Toast.makeText(this, "Repeat 1 time", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    // Ở đây bạn có thể viết logic để lặp lại bài hát 2 lần
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                    binding.repeatIcon.setImageResource(R.drawable.repeat_2)
                    Toast.makeText(this, "Repeat 2 times", Toast.LENGTH_SHORT).show()
                }
                3 -> {
                    // Logic để lặp 3 lần
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                    binding.repeatIcon.setImageResource(R.drawable.repeat_2)
                    Toast.makeText(this, "Repeat 3 times", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.repeatIcon.setOnLongClickListener {
            repeatCount = 0 // Reset số lần lặp về 0
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF // Tắt chế độ lặp
            binding.repeatIcon.setImageResource(R.drawable.repeat)
            Toast.makeText(this, "Repeat off", Toast.LENGTH_SHORT).show()
            true // Trả về true để sự kiện được xử lý
        }

        findViewById<View>(R.id.more_button).setOnClickListener {
            showBottomSheetMenuPlayerview()
        }

        // Trong PlayerActivity
        binding.singerImageView.setOnClickListener {
            val intent = Intent(this, DetailSingerActivity::class.java)
            MyExoplayer.getCurrentSong()?.let { song ->
                intent.putExtra("singerName", song.subtitle)
                intent.putExtra("singerImageUrl", song.singerUrl)
                intent.putExtra("singerDetail", song.detailSinger)
            }
            startActivity(intent)
        }

    }
    private fun showTimerBottomSheet() {

        // Khởi tạo BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)

        // Inflate layout từ file XML
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_timer, null)

        // Thiết lập nội dung cho BottomSheetDialog
        bottomSheetDialog.setContentView(view)
        // Lắng nghe sự kiện khi người dùng click vào các tùy chọn
        view.findViewById<TextView>(R.id.option_5_minutes).setOnClickListener {
            startTimer(5) // Bắt đầu hẹn giờ 5 phút
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog
        }

        view.findViewById<TextView>(R.id.option_10_minutes).setOnClickListener {
            startTimer(10) // Bắt đầu hẹn giờ 10 phút
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog
        }

        view.findViewById<TextView>(R.id.option_20_minutes).setOnClickListener {
            startTimer(20) // Bắt đầu hẹn giờ 20 phút
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog
        }

        view.findViewById<TextView>(R.id.option_30_minutes).setOnClickListener {
            startTimer(30) // Bắt đầu hẹn giờ 30 phút
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog
        }
        view.findViewById<TextView>(R.id.option_45_minutes).setOnClickListener {
            startTimer(45) // Bắt đầu hẹn giờ 30 phút
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog
        }
        view.findViewById<TextView>(R.id.option_45_minutes).setOnClickListener {
            startTimer(60) // Bắt đầu hẹn giờ 30 phút
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog
        }

        // Hiển thị BottomSheetDialog
        bottomSheetDialog.show()
    }


    // Hàm để bắt đầu hẹn giờ (tùy chỉnh theo logic của bạn)
    private fun startTimer(minutes: Int) {
        // Đổi icon sang time_2 khi hẹn giờ được kích hoạt
        binding.timerIcon.setImageResource(R.drawable.time_2)
        isTimerActive = true

        // Thông báo hẹn giờ được thiết lập
        Toast.makeText(this, "Hẹn giờ: $minutes phút", Toast.LENGTH_SHORT).show()

        val delayMillis = minutes * 60 * 1000L
        val handler = android.os.Handler()
        handler.postDelayed({
            exoPlayer.pause() // Dừng phát nhạc sau khi hết giờ

            // Thông báo hẹn giờ kết thúc
            Toast.makeText(this, "Hẹn giờ kết thúc, đã dừng phát nhạc.", Toast.LENGTH_SHORT).show()

            // Đổi lại icon về time khi hẹn giờ kết thúc
            binding.timerIcon.setImageResource(R.drawable.time)
            isTimerActive = false
        }, delayMillis)
    }
    private fun showBottomSheetMenuPlayerview() {
        // Tạo một BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)


        // Inflate layout cho bottom sheet
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_menu_playerview, null)


        // Gắn layout vào BottomSheetDialog
        bottomSheetDialog.setContentView(bottomSheetView)


        bottomSheetView.findViewById<TextView>(R.id.menu_item_2).setOnClickListener {
            // Xử lý sự kiện cho Option 2
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<TextView>(R.id.menu_item_3).setOnClickListener {
            // Xử lý sự kiện cho Option 3
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<TextView>(R.id.add_playlist).setOnClickListener {
            showAddToPlaylistDialog() // Gọi hàm để hiển thị danh sách playlist
            bottomSheetDialog.dismiss() // Đóng dialog hiện tại
        }
        bottomSheetView.findViewById<TextView>(R.id.menu_item_2).setOnClickListener {
            addToFavorites()
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<TextView>(R.id.menu_item_3).setOnClickListener {
            shareSong() // Gọi hàm chia sẻ bài hát
            bottomSheetDialog.dismiss() // Đóng dialog sau khi chọn
        }


        bottomSheetView.findViewById<TextView>(R.id.share_qr_button).setOnClickListener {
            shareSongViaQRCode()
            bottomSheetDialog.dismiss() // Đóng dialog sau khi chọn
        }

        // Hiển thị BottomSheetDialog
        bottomSheetDialog.show()

        bottomSheetView.findViewById<TextView>(R.id.report_button).setOnClickListener {
            showReportDialog() // Gọi hàm hiển thị dialog báo cáo
            bottomSheetDialog.dismiss() // Đóng BottomSheetDialog sau khi chọn
        }
    }
    private fun showAddToPlaylistDialog() {
        // Khởi tạo BottomSheetDialog mới
        val playlistBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)

        // Inflate layout từ file XML (ScrollView chứa danh sách playlist)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_playlist_selection, null)
        val playlistContainer = view.findViewById<LinearLayout>(R.id.playlist_container)

        // Kết nối tới Firestore để lấy danh sách playlist
        val firestore = FirebaseFirestore.getInstance()
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        // Kiểm tra userUid có hợp lệ
        if (userUid != null) {
            firestore.collection("playlists")
                .whereEqualTo("userUid", userUid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val playlists = querySnapshot.documents.map { doc ->
                        doc.toObject(PlaylistModel::class.java)?.copy(id = doc.id)
                    }.filterNotNull() // Loại bỏ giá trị null

                    if (playlists.isEmpty()) {
                        // Hiển thị thông báo khi không có playlist
                        val noPlaylistTextView = TextView(this)
                        noPlaylistTextView.text = "Chưa có playlist nào."
                        noPlaylistTextView.textSize = 16f
                        noPlaylistTextView.setTextColor(resources.getColor(android.R.color.white, theme))
                        noPlaylistTextView.setPadding(50, 50, 50, 20)
                        playlistContainer.addView(noPlaylistTextView)

                        // Thêm tùy chọn "Tạo playlist"
                        val createPlaylistTextView = TextView(this)
                        createPlaylistTextView.text = "Tạo playlist +"
                        createPlaylistTextView.textSize = 16f
                        createPlaylistTextView.setTextColor(resources.getColor(android.R.color.white, theme))
                        createPlaylistTextView.setPadding(50, 20, 50, 50)
                        createPlaylistTextView.setOnClickListener {
                            val intent = Intent(this, AddPlaylistActivity::class.java)
                            startActivity(intent)
                            playlistBottomSheetDialog.dismiss() // Đóng dialog sau khi chuyển hướng
                        }
                        playlistContainer.addView(createPlaylistTextView)
                    } else {
                        // Thêm danh sách playlist vào view
                        playlists.forEach { playlist ->
                            val textView = TextView(this)
                            textView.text = playlist.name
                            textView.textSize = 16f
                            textView.setTextColor(resources.getColor(android.R.color.white, theme))
                            textView.setPadding(50, 50, 50, 20)
                            textView.setOnClickListener {
                                addSongToPlaylist(playlist)
                                playlistBottomSheetDialog.dismiss()
                            }
                            playlistContainer.addView(textView)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Không thể tải danh sách playlist: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
        }

        // Gắn layout vào BottomSheetDialog
        playlistBottomSheetDialog.setContentView(view)

        // Hiển thị BottomSheetDialog
        playlistBottomSheetDialog.show()
    }

    private fun addSongToPlaylist(playlist: PlaylistModel) {
        val firestore = FirebaseFirestore.getInstance()

        val currentSong = MyExoplayer.getCurrentSong()
        if (currentSong == null) {
            Toast.makeText(this, "Không thể thêm bài hát: Bài hát hiện tại không tồn tại.", Toast.LENGTH_SHORT).show()
            return
        }

        val playlistRef = firestore.collection("playlists").document(playlist.id)
        playlistRef.update("songs", FieldValue.arrayUnion(currentSong.id))
            .addOnSuccessListener {
                Toast.makeText(this, "Đã thêm ${currentSong.title} vào ${playlist.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Không thể thêm bài hát: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun addToFavorites() {
        val firestore = FirebaseFirestore.getInstance()
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        if (userUid == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào yêu thích.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentSong = MyExoplayer.getCurrentSong()
        if (currentSong == null) {
            Toast.makeText(this, "Không thể thêm bài hát: Không có bài hát đang phát.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tìm tài liệu "favorites" của người dùng
        val favoriteRef = firestore.collection("favorites").whereEqualTo("userUid", userUid)

        favoriteRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Nếu danh sách yêu thích đã tồn tại, thêm bài hát vào
                val document = querySnapshot.documents.first()
                val docId = document.id
                firestore.collection("favorites").document(docId)
                    .update("songs", FieldValue.arrayUnion(currentSong.id))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã thêm bài hát vào yêu thích.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Lỗi khi thêm bài hát: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Nếu chưa có danh sách yêu thích, tạo tài liệu mới
                val newFavorite = mapOf(
                    "userUid" to userUid,
                    "songs" to listOf(currentSong.id)
                )

                firestore.collection("favorites").add(newFavorite)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Danh sách yêu thích được tạo và bài hát đã được thêm.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Lỗi khi tạo danh sách yêu thích: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Lỗi khi kiểm tra danh sách yêu thích: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun shareSong() {
        val currentSong = MyExoplayer.getCurrentSong()
        if (currentSong == null) {
            Toast.makeText(this, "Không có bài hát nào để chia sẻ.", Toast.LENGTH_SHORT).show()
            return
        }

        // Base dynamic link domain
        val dynamicLinkDomain = "https://feelingmusic.page.link"

        // Tạo Dynamic Link
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse("https://feelingmusic.com/song?id=${currentSong.id}")
            domainUriPrefix = dynamicLinkDomain
            androidParameters("com.example.musicstream") {
                minimumVersion = 1 // Đặt phiên bản tối thiểu của ứng dụng
            }
            socialMetaTagParameters {
                title = currentSong.title
                description = "Nghe bài hát ${currentSong.title} trên FeelingMusic!"
                imageUrl = Uri.parse(currentSong.coverUrl)
            }
        }

        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { result ->
            val shortLink = result.shortLink.toString()

            // Intent chia sẻ
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ bài hát")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🎵 Nghe bài hát này: ${currentSong.title} - ${currentSong.subtitle}\n" +
                            "📌 Link bài hát: $shortLink"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài hát qua"))
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Không thể tạo link chia sẻ: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun recordListeningHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val currentSong = MyExoplayer.getCurrentSong()

        if (currentSong != null) {
            val historyData = mapOf(
                "userUid" to userId,
                "songId" to currentSong.id,
                "timestamp" to FieldValue.serverTimestamp() // Lưu thời gian nghe
            )

            db.collection("usage_history")
                .add(historyData)
                .addOnSuccessListener {
                    Log.d("ListeningHistory", "Ghi nhận lịch sử nghe thành công")
                }
                .addOnFailureListener { e ->
                    Log.e("ListeningHistory", "Lỗi khi ghi nhận lịch sử: ", e)
                }
        }
    }
    private fun playNextRandomSong() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "Không có bài hát nào để phát.", Toast.LENGTH_SHORT).show()
            return
        }

        // Chọn bài hát ngẫu nhiên khác
        val randomSong = songList.random() // Lấy bài hát ngẫu nhiên từ danh sách
        try {
            MyExoplayer.startPlaying(this, randomSong)
            updateUIWithSong(randomSong) // Cập nhật giao diện với bài hát mới
            saveListeningHistoryToFirestore(randomSong)
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error playing next song: ${e.message}")
            Toast.makeText(this, "Lỗi khi phát bài hát tiếp theo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIWithSong(song: SongModel) {
        binding.songTitleTextView.text = song.title
        binding.songSubtitleTextView.text = song.subtitle
        binding.songSingerTextView.text = song.subtitle
        binding.singerTextView.text = song.subtitle
        binding.songLyricTextView.text = song.lyric
        binding.songDetailSingerTextView.text = song.detailSinger
        binding.songCountTextView.text = "${song.count}"

        Glide.with(binding.songCoverImageView).load(song.coverUrl)
            .apply(RequestOptions().transform(RoundedCorners(50)))
            .into(binding.songCoverImageView)
        Glide.with(binding.backgroundImageView).load(song.coverUrl)
            .apply(RequestOptions().transform(RoundedCorners(50)))
            .into(binding.backgroundImageView)

        Glide.with(binding.singerImageView).load(song.singerUrl)
            .into(binding.singerImageView)
    }
    private fun loadSongs() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("songs")
            .get()
            .addOnSuccessListener { querySnapshot ->
                songList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(SongModel::class.java)?.copy(id = document.id)
                }
                if (songList.isEmpty()) {
                    Toast.makeText(this, "Không có bài hát nào trong danh sách.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Lỗi khi tải danh sách bài hát: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun saveListeningHistoryToFirestore(song: SongModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val historyEntry = hashMapOf(
            "userUid" to userId,
            "songId" to song.id,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("usage_history")
            .add(historyEntry)
            .addOnSuccessListener {
                Log.d("Firestore", "Lịch sử phát nhạc đã được lưu thành công!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Không thể ghi lịch sử phát nhạc!", e)
                Toast.makeText(this, "Không thể ghi nhận thông tin!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.removeListener(playerListener)
    }

    private fun shareSongViaQRCode() {
        val currentSong = MyExoplayer.getCurrentSong()
        if (currentSong == null) {
            Toast.makeText(this, "Không có bài hát nào để chia sẻ.", Toast.LENGTH_SHORT).show()
            return
        }

        val dynamicLinkDomain = "https://feelingmusic.page.link"
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse("https://feelingmusic.com/song?id=${currentSong.id}")
            domainUriPrefix = dynamicLinkDomain
            androidParameters("com.example.musicstream") {
                minimumVersion = 1
            }
        }

        Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { result ->
            val shortLink = result.shortLink.toString()
            showQRCodeDialog(shortLink) // Hiển thị mã QR
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Không thể tạo mã QR: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showQRCodeDialog(url: String) {
        val qrCodeBitmap = generateQRCode(url)
        if (qrCodeBitmap != null) {
            val dialog = AlertDialog.Builder(this)
            val imageView = ImageView(this).apply {
                setImageBitmap(qrCodeBitmap)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(50, 50, 50, 50)
            }
            dialog.setView(imageView)
            dialog.setPositiveButton("Đóng") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            dialog.show()
        } else {
            Toast.makeText(this, "Không thể tạo mã QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateQRCode(url: String): Bitmap? {
        return try {
            val width = 900
            val height = 900
            val bitMatrix = MultiFormatWriter().encode(
                url, BarcodeFormat.QR_CODE, width, height
            )
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
    private fun setupPlayerView() {
        // Lấy instance của ExoPlayer và gán cho PlayerView
        exoPlayer = MyExoplayer.getInstance()!!
        binding.playerView.player = exoPlayer
        binding.playerView.showController() // Hiển thị controller
        exoPlayer.addListener(playerListener)
    }
    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.report_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val reasonsGroup = dialogView.findViewById<RadioGroup>(R.id.report_reasons_group)
        val otherReasonInput = dialogView.findViewById<EditText>(R.id.other_reason_input)
        val submitButton = dialogView.findViewById<Button>(R.id.submit_report_button)

        reasonsGroup.setOnCheckedChangeListener { _, checkedId ->
            otherReasonInput.visibility =
                if (checkedId == R.id.reason_other) View.VISIBLE else View.GONE
        }

        submitButton.setOnClickListener {
            val selectedReasonId = reasonsGroup.checkedRadioButtonId
            val reason = when (selectedReasonId) {
                R.id.reason_wrong_info -> "Sai thông tin bài hát"
                R.id.reason_poor_audio -> "Âm thanh bài hát kém"
                R.id.reason_other -> otherReasonInput.text.toString()
                else -> null
            }

            if (reason.isNullOrEmpty()) {
                Toast.makeText(this, "Vui lòng chọn lý do báo cáo", Toast.LENGTH_SHORT).show()
            } else {
                sendReportToFirebase(reason)
                dialog.dismiss()
                Toast.makeText(this, "Báo cáo của bạn đã được gửi", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun sendReportToFirebase(reason: String) {
        val db = FirebaseFirestore.getInstance()
        val currentSong = MyExoplayer.getCurrentSong()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentSong != null && currentUser != null) {
            val report = hashMapOf(
                "songId" to currentSong.id,
                "songTitle" to currentSong.title,
                "reason" to reason,
                "reportedAt" to FieldValue.serverTimestamp(),
                "reportedBy" to currentUser.uid,
                "email" to currentUser.email
            )

            db.collection("reports")
                .add(report)
                .addOnSuccessListener {
                    Log.d("Report", "Báo cáo đã được gửi thành công")
                }
                .addOnFailureListener { e ->
                    Log.e("Report", "Lỗi khi gửi báo cáo: $e")
                }
        } else {
            Toast.makeText(this, "Không tìm thấy bài hát hoặc thông tin người dùng", Toast.LENGTH_SHORT).show()
        }
    }




//    private fun playNextRandomSong() {
//        if (songList.isEmpty()) {
//            Toast.makeText(this, "Không có bài hát nào để phát.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val nextIndex = (songList.indices - currentSongIndex).randomOrNull() ?: return
//        playSongAtIndex(nextIndex)
//    }
//
//
//    private fun updateUIWithSong(song: SongModel) {
//        binding.songTitleTextView.text = song.title
//        binding.songSubtitleTextView.text = song.subtitle
//        binding.songSingerTextView.text = song.subtitle
//        binding.singerTextView.text = song.subtitle
//        binding.songLyricTextView.text = song.lyric
//        binding.songDetailSingerTextView.text = song.detailSinger
//        binding.songCountTextView.text = "${song.count}"
//
//        Glide.with(binding.songCoverImageView).load(song.coverUrl)
//            .apply(RequestOptions().transform(RoundedCorners(50)))
//            .into(binding.songCoverImageView)
//
//        Glide.with(binding.singerImageView).load(song.singerUrl)
//            .into(binding.singerImageView)
//    }
//    private fun loadSongs() {
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.collection("songs")
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                val songs = querySnapshot.documents.mapNotNull { document ->
//                    document.toObject(SongModel::class.java)?.copy(id = document.id)
//                }
//                if (songs.isNotEmpty()) {
//                    setSongList(songs)
//                } else {
//                    Toast.makeText(this, "Không có bài hát nào trong danh sách.", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(this, "Lỗi khi tải danh sách bài hát: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//
//    fun playSongAtIndex(index: Int) {
//        if (index < 0 || index >= songList.size) {
//            Toast.makeText(this, "Vị trí bài hát không hợp lệ.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val song = songList[index]
//        try {
//            MyExoplayer.startPlaying(this, song) // Phát bài hát tại vị trí chỉ định
//            updateUIWithSong(song) // Cập nhật giao diện
//            saveListeningHistoryToFirestore(song) // Lưu lịch sử phát
//            currentSongIndex = index // Lưu trạng thái bài hát hiện tại
//        } catch (e: Exception) {
//            Log.e("PlayerActivity", "Error playing song at index: ${e.message}")
//            Toast.makeText(this, "Lỗi khi phát bài hát.", Toast.LENGTH_SHORT).show()
//        }
//    }

}
