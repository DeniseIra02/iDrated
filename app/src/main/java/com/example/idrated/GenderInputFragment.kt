package com.example.idrated

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class GenderInputFragment : Fragment(R.layout.fragment_gender_input) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reference to buttons
        val maleButton: Button = view.findViewById(R.id.maleButton)
        val femaleButton: Button = view.findViewById(R.id.femaleButton)

        // Set listeners to handle button clicks
        maleButton.setOnClickListener {
            // Handle male button click
            showGenderSelection("Male")
        }

        femaleButton.setOnClickListener {
            // Handle female button click
            showGenderSelection("Female")
        }
    }

    // Function to display selected gender
    private fun showGenderSelection(gender: String) {
        Toast.makeText(requireContext(), "Selected Gender: $gender", Toast.LENGTH_SHORT).show()
        // You can replace the Toast with any other action, e.g., saving the gender or updating UI.
    }
}
