package com.example.idrated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.idrated.databinding.FragmentAgeInputBinding

class AgeInputFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: FragmentAgeInputBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAgeInputBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Set up SeekBar listener to display selected age
        binding.ageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ageText.text = "Age: $progress"  // Update the displayed age
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing for now
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing for now
            }
        })

        // Save the selected age to Realtime Database when the user finishes choosing their age
        binding.ageSeekBar.setOnTouchListener { _, _ ->
            val age = binding.ageSeekBar.progress
            saveAgeToDatabase(age)
            true
        }

        return binding.root
    }

    // Function to save the age to the Realtime Database
    private fun saveAgeToDatabase(age: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users/$userId/age")
            userRef.setValue(age)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Age saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save age", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
