package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.idrated.databinding.ActivityGoalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GoalActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityGoalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Load the user's saved water goal and consumed amount when the activity starts
        loadWaterGoalAndConsumed()

        binding.btnSetDailyGoal.setOnClickListener {
            val intent = Intent(this, WaterInputActivity::class.java)
            startActivity(intent)
        }

        binding.addWaterButton.setOnClickListener {
            val waterIntake = binding.waterInput.text.toString().toIntOrNull()
            if (waterIntake != null && waterIntake > 0) {
                updateWaterConsumed(waterIntake)
                binding.waterInput.text.clear() // Clear the input after adding
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        binding.LogoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun saveWaterGoal(goal: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.child("users").child(userId)
            userRef.child("waterGoal").setValue(goal)
            userRef.child("waterConsumed").setValue(0) // Initialize consumed to 0
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal saved successfully!", Toast.LENGTH_SHORT).show()
                    updateGoalDisplay(goal, 0) // Update display with initial values
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadWaterGoalAndConsumed() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savedGoal = snapshot.child("waterGoal").getValue(Int::class.java) ?: 0
                    val consumed = snapshot.child("waterConsumed").getValue(Int::class.java) ?: 0
                    updateGoalDisplay(savedGoal, consumed)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GoalActivity, "Failed to load goal", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateGoalDisplay(goal: Int, consumed: Int) {
        binding.goalDisplay.text = goal.toString()
        binding.goalConsumed.text = consumed.toString()
        updatePercentage(goal, consumed)
        updateProgressBar(goal, consumed) // Update the progress bar
    }

    private fun updatePercentage(goal: Int, consumed: Int) {
        val percentage = if (goal > 0) {
            ((consumed.toDouble() / goal) * 100).toInt()
        } else {
            0
        }
        binding.percent.text = "$percentage%"
    }

    private fun updateProgressBar(goal: Int, consumed: Int) {
        binding.progressBar.max = goal // Set the max value to the water goal
        binding.progressBar.progress = consumed // Set the current progress to water consumed
    }

    private fun updateWaterConsumed(waterIntake: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentConsumed = snapshot.child("waterConsumed").getValue(Int::class.java) ?: 0
                    val goal = snapshot.child("waterGoal").getValue(Int::class.java) ?: 0
                    val newConsumed = (currentConsumed + waterIntake).coerceAtMost(goal)

                    userRef.child("waterConsumed").setValue(newConsumed)
                        .addOnSuccessListener {
                            Toast.makeText(this@GoalActivity, "Water intake updated!", Toast.LENGTH_SHORT).show()
                            updateGoalDisplay(goal, newConsumed)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@GoalActivity, "Failed to update water intake", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GoalActivity, "Failed to retrieve water consumed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
