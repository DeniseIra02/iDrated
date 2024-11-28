package com.example.idrated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment

class GenderInputFragment : Fragment(R.layout.fragment_gender_input) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maleRadioButton: RadioButton = view.findViewById(R.id.maleRadioButton)
        val femaleRadioButton: RadioButton = view.findViewById(R.id.femaleRadioButton)
        val otherRadioButton: RadioButton = view.findViewById(R.id.otherRadioButton)
        val otherEditText: EditText = view.findViewById(R.id.otherGenderEditText)

        // Hide the other input field initially
        otherEditText.visibility = View.GONE

        // Show the "Other" input field when the "Other" radio button is selected
        otherRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                otherEditText.visibility = View.VISIBLE
            } else {
                otherEditText.visibility = View.GONE
            }
        }
    }
}
