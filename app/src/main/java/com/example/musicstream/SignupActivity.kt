package com.example.musicstream

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.musicstream.databinding.ActivitySignupBinding
import com.example.musicstream.models.FavoriteModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.createAccountBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            val confirmPassword = binding.confirmPasswordEdittext.text.toString()

            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(),email)){
                binding.emailEdittext.setError("Invalid email")
                return@setOnClickListener
            }

            if (password.length < 6){
                binding.passwordEdittext.setError("Length should be 6 chars")
                return@setOnClickListener
            }

            if (!password.equals(confirmPassword)){
                binding.confirmPasswordEdittext.setError("Password not matched")
                return@setOnClickListener
            }

            createAccountWithFirebase(email,password)

        }

        binding.gotoLoginBtn.setOnClickListener {
            finish()
        }

    }

    fun createAccountWithFirebase(email: String, password: String) {
        setInProgress(true)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                setInProgress(false)

                // Get user UID
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                // Create default favorite document
                val favorite = FavoriteModel(
                    id = uid,
                    songs = emptyList(),
                    userUid = uid
                )

                db.collection("favorites").document(uid).set(favorite)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "User created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Failed to create favorite document", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                setInProgress(false)
                Toast.makeText(applicationContext, "Create account failed", Toast.LENGTH_SHORT).show()
            }
    }


    fun setInProgress(inProgress : Boolean){
        if (inProgress){
            binding.createAccountBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.createAccountBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

}