package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.idrated.databinding.ActivityAgeSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AgeSelectionActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityAgeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAgeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Create age list from 1 to 150
        val ageList = (1..150).map { it.toString() }.toList()

        // Set up a spinner (dropdown) for age selection
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ageList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ageSpinner.adapter = adapter

        // Set up the button to save age to Firestore and navigate to the next page
        binding.btnSaveAge.setOnClickListener {
            val selectedAge = binding.ageSpinner.selectedItem.toString().toInt()

            saveAgeAndNavigate(selectedAge)
        }
    }

    // Function to save age to Firestore and navigate to SetGoalActivity
    private fun saveAgeAndNavigate(age: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Set the age in Firestore for the current user
            db.collection("users").document(userId)
                .update("age", age)
                .addOnSuccessListener {
                    Toast.makeText(this, "Age saved successfully!", Toast.LENGTH_SHORT).show()

                    // Navigate to SetGoalActivity
                    val intent = Intent(this, ActivityLevelSelectionActivity::class.java)  // Ensure Intent is properly used here
                    startActivity(intent)
                    finish() // Finish AgeSelectionActivity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save age", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
