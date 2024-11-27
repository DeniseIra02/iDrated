package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeatherConditionActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_condition)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize buttons from the XML layout
        findViewById<Button>(R.id.btn_hot).setOnClickListener {
            updateWaterGoal(500, "Hot")
        }

        findViewById<Button>(R.id.btn_temperate).setOnClickListener {
            updateWaterGoal(500, "Temperate")
        }

        findViewById<Button>(R.id.btn_cold).setOnClickListener {
            updateWaterGoal(0, "Cold")
        }
    }

    private fun updateWaterGoal(addition: Int, weatherCondition: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val currentGoal = document.getLong("waterGoal")?.toInt() ?: 0
                        val newGoal = currentGoal + addition

                        db.collection("users").document(userId)
                            .update(mapOf("waterGoal" to newGoal, "weatherCondition" to weatherCondition))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Weather Condition Saved!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, SetGoalActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update water goal", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
