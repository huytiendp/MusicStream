package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        binding.exitButton.setOnClickListener {
            finish() // Đóng activity khi nút thoát được bấm
        }
        binding.optionBtn.setOnClickListener {
            showPopupWindow()
        }
        // Chỉnh sửa mật khẩu
        binding.changePasswordOption.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
        binding.root.findViewById<TextView>(R.id.statistics_option).setOnClickListener {
            startActivity(Intent(this, TimeStatisticsActivity::class.java))
        }
        binding.root.findViewById<TextView>(R.id.listenhistory_option).setOnClickListener {
            startActivity(Intent(this, ListeningHistoryActivity::class.java))
        }
        // Đăng xuất
        binding.logoutOption.setOnClickListener {
            logout()
        }
    }
    fun showPopupWindow() {
        // Inflate layout from XML
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_menu_layout, null)

        val popupWindow = PopupWindow(view, resources.displayMetrics.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Set properties for PopupWindow
        popupWindow.isFocusable = true
        popupWindow.update()

        // Áp dụng cả enter và exit animation cho PopupWindow
        popupWindow.animationStyle = R.style.PopupAnimation

        // Show the PopupWindow at the bottom of the screen with animation
        popupWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)

        // Handle click events for menu options
        view.findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            popupWindow.dismiss()  // Close the popup after clicking
        }
        view.findViewById<LinearLayout>(R.id.home_option).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))  // Redirect to MainActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }

        view.findViewById<TextView>(R.id.all_songs).setOnClickListener {
            startActivity(Intent(this, AllSongsActivity::class.java))  // Redirect to AllSongsActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }

    }


    fun logout() {
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
