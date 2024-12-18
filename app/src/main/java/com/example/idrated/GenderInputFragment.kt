package com.example.idrated

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class GenderInputFragment : Fragment(R.layout.fragment_gender_input) {

    private lateinit var maleButton: Button
    private lateinit var femaleButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reference to buttons
        maleButton = view.findViewById(R.id.maleButton)
        femaleButton = view.findViewById(R.id.femaleButton)

        // Set listeners to handle button clicks
        maleButton.setOnClickListener {
            setSelectedButton(maleButton, femaleButton)
            showGenderSelection("Male")
        }

        femaleButton.setOnClickListener {
            setSelectedButton(femaleButton, maleButton)
            showGenderSelection("Female")
        }
    }

    // Function to handle button appearance
    private fun setSelectedButton(selectedButton: Button, otherButton: Button) {
        // Highlight the selected button
        selectedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        selectedButton.setTextColor(Color.WHITE)
        selectedButton.elevation = 8f

        // Reset the other button
        otherButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        otherButton.setTextColor(Color.WHITE)
        otherButton.elevation = 2f
    }

    // Function to display selected gender
    private fun showGenderSelection(gender: String) {
        Toast.makeText(requireContext(), "Selected Gender: $gender", Toast.LENGTH_SHORT).show()
        // You can replace the Toast with any other action, e.g., saving the gender or updating UI.
    }
}