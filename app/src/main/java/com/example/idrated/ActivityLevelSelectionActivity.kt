package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityLevelSelectionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_level_selection)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnSedentary = findViewById<Button>(R.id.btnSedentary)
        val btnLightlyActive = findViewById<Button>(R.id.btnLightlyActive)
        val btnModeratelyActive = findViewById<Button>(R.id.btnModeratelyActive)
        val btnVeryActive = findViewById<Button>(R.id.btnVeryActive)

        btnSedentary.setOnClickListener {
            updateWaterGoal("Sedentary", 0)
        }

        btnLightlyActive.setOnClickListener {
            updateWaterGoal("Lightly Active", 250)
        }

        btnModeratelyActive.setOnClickListener {
            updateWaterGoal("Moderately Active", 500)
        }

        btnVeryActive.setOnClickListener {
            updateWaterGoal("Very Active", 750)
        }
    }

    private fun updateWaterGoal(activityLevel: String, additionalWater: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val currentWaterGoal = document.getLong("waterGoal")?.toInt() ?: 0
                        val newWaterGoal = currentWaterGoal + additionalWater

                        // Update the water goal in Firestore
                        db.collection("users").document(userId)
                            .update("waterGoal", newWaterGoal, "activityLevel", activityLevel)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Activity level saved!", Toast.LENGTH_SHORT).show()

                                // Navigate to SetGoalActivity
                                val intent = Intent(this, WeatherConditionActivity::class.java)
                                startActivity(intent)
                                finish() // Finish the current activity
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update water goal", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
