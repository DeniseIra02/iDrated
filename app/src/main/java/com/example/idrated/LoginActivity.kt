package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.idrated.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Login button logic
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            loginUser(email, password)
        }

        // Register link logic
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Toggle password visibility
        binding.showHidePasswordIcon.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    db.collection("users").document(userId!!)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val userData = document.data
                                Toast.makeText(this, "Welcome, ${userData?.get("email")}", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, GoalActivity::class.java)) // Redirect to GoalActivity
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            binding.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.showHidePasswordIcon.setImageResource(R.drawable.ic_visibility_off)
        } else {
            // Show password
            binding.passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.showHidePasswordIcon.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible

        // Move the cursor to the end of the text
        binding.passwordInput.setSelection(binding.passwordInput.text.length)
    }
}
