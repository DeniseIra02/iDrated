package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.idrated.databinding.ActivityGenderSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GenderSelectionActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityGenderSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGenderSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Male Button Click Listener
        binding.btnMale.setOnClickListener {
            setWaterGoalAndNavigate(3700, "Male")
        }

        // Female Button Click Listener
        binding.btnFemale.setOnClickListener {
            setWaterGoalAndNavigate(2700, "Female")
        }
    }

    // Function to set water goal based on gender and navigate to SetGoalActivity
    private fun setWaterGoalAndNavigate(goal: Int, gender: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Set the water goal and gender in Firestore for the current user
            db.collection("users").document(userId)
                .update(
                    "waterGoal", goal,
                    "waterConsumed", 0,
                    "gender", gender // Save gender to Firestore
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Gender set successfully!", Toast.LENGTH_SHORT).show()

                    // Navigate to SetGoalActivity
                    val intent = Intent(this, AgeSelectionActivity::class.java)
                    startActivity(intent)
                    finish() // Finish GenderSelectionActivity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to set water goal and gender", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
