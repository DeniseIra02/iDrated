package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import android.widget.ImageView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.idrated.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize the password toggle buttons
        val passwordToggle: ImageView = binding.registerPasswordVisibilityToggle
        val confirmPasswordToggle: ImageView = binding.confirmPasswordVisibilityToggle

        val passwordInput: EditText = binding.registerPasswordInput
        val confirmPasswordInput: EditText = binding.confirmPasswordInput

        // Toggle password visibility
        passwordToggle.setOnClickListener {
            togglePasswordVisibility(passwordInput, passwordToggle)
        }

        // Toggle confirm password visibility
        confirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility(confirmPasswordInput, confirmPasswordToggle)
        }

        binding.registerButton.setOnClickListener {
            val email = binding.registerEmailInput.text.toString()
            val password = binding.registerPasswordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            // Validate email format
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (password == confirmPassword) {
                    registerUser(email, password)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val userMap = hashMapOf(
                        "email" to email
                    )
                    db.collection("users").document(userId!!)
                        .set(userMap)
                        .addOnSuccessListener {
                            // Show success message
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                            // Start OnboardingActivity after successful registration
                            val intent = Intent(this, OnboardingActivity::class.java)
                            startActivity(intent)
                            finish()  // Optional: Finish the RegisterActivity so user cannot return to it
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user details", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Log the specific error
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Toast.makeText(this, "Registration failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun togglePasswordVisibility(passwordInput: EditText, passwordToggle: ImageView) {
        if (passwordInput.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show password
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_visibility)
        } else {
            // Hide password
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_visibility_off)
        }
        // Move the cursor to the end of the text
        passwordInput.setSelection(passwordInput.text.length)
    }
}
