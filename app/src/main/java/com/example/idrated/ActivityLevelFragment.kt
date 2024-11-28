package com.example.idrated

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class ActivityLevelFragment : Fragment(R.layout.fragment_activity_level) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lowActivityButton: Button = view.findViewById(R.id.lowActivityButton)
        val mediumActivityButton: Button = view.findViewById(R.id.mediumActivityButton)
        val highActivityButton: Button = view.findViewById(R.id.highActivityButton)

        // Set button listeners and store the selected activity level
        lowActivityButton.setOnClickListener {
            // Handle low activity level selection
        }

        mediumActivityButton.setOnClickListener {
            // Handle medium activity level selection
        }

        highActivityButton.setOnClickListener {
            // Handle high activity level selection
        }
    }
}

