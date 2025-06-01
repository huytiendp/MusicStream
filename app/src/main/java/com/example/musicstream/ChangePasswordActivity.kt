package com.example.musicstream

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicstream.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
        // Setup for popup menu on option button click
        binding.optionBtn.setOnClickListener {
            showPopupWindow()
        }

        binding.exitButton.setOnClickListener {
            finish() // Đóng activity khi nút thoát được bấm
        }

        binding.changePasswordBtn.setOnClickListener {
            val currentPassword = binding.currentPasswordEdittext.text.toString()
            val newPassword = binding.newPasswordEdittext.text.toString()
            val confirmPassword = binding.confirmPasswordEdittext.text.toString()

            if (currentPassword.isEmpty()) {
                binding.currentPasswordEdittext.error = "Current password is required"
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                binding.newPasswordEdittext.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                binding.confirmPasswordEdittext.error = "Passwords do not match"
                return@setOnClickListener
            }

            changePassword(currentPassword, newPassword)
        }
    }

    fun showPopupWindow() {
        // Inflate layout from XML
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_menu_layout, null)

        // Tính toán chiều cao của popup để nó chiếm 1/3 chiều cao màn hình
        val popupHeight = resources.displayMetrics.heightPixels / 3

        // Create PopupWindow with the inflated layout
        val popupWindow = PopupWindow(view, resources.displayMetrics.widthPixels, popupHeight)

        // Set properties for PopupWindow
        popupWindow.isFocusable = true
        popupWindow.update()

        // Áp dụng cả enter và exit animation cho PopupWindow
        popupWindow.animationStyle = R.style.PopupAnimation

        // Show the PopupWindow at the bottom of the screen with animation
        popupWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)

        // Handle click events for menu options
        view.findViewById<TextView>(R.id.logout_option).setOnClickListener {
            logout()  // Call logout function when logout option is clicked
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }

        view.findViewById<TextView>(R.id.change_password_option).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))  // Redirect to ChangePasswordActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }

        view.findViewById<TextView>(R.id.home_option).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))  // Redirect to MainActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }

        view.findViewById<TextView>(R.id.all_songs).setOnClickListener {
            startActivity(Intent(this, AllSongsActivity::class.java))  // Redirect to AllSongsActivity
            popupWindow.dismiss()  // Popup sẽ biến mất với hiệu ứng trượt xuống
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = it.email
            if (email != null) {
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                setInProgress(true)

                // Re-authenticate the user
                it.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Update the password
                        it.updatePassword(newPassword).addOnCompleteListener { task ->
                            setInProgress(false)
                            if (task.isSuccessful) {
                                Toast.makeText(applicationContext, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(applicationContext, "Password change failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        setInProgress(false)
                        Toast.makeText(applicationContext, "Re-authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.changePasswordBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.changePasswordBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}
