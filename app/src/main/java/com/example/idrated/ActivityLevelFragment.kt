package com.example.idrated

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ActivityLevelFragment : Fragment(R.layout.fragment_activity_level) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the buttons and caption TextViews
        val sedentaryButton: Button = view.findViewById(R.id.sedentaryButton)
        val moderateActivityButton: Button = view.findViewById(R.id.moderateActivityButton)
        val highlyActiveButton: Button = view.findViewById(R.id.highlyActiveButton)
        val activityCaptionTextView: TextView = view.findViewById(R.id.activityCaptionTextView)
        val activityDescriptionTextView: TextView = view.findViewById(R.id.activityDescriptionTextView)

        // Set up button click listeners
        sedentaryButton.setOnClickListener {
            activityCaptionTextView.text = "You selected Sedentary"
            activityDescriptionTextView.text = "Sedentary: Little to no physical activity. Often involves sitting or lying down for most of the day."
        }

        moderateActivityButton.setOnClickListener {
            activityCaptionTextView.text = "You selected Moderate Activity"
            activityDescriptionTextView.text = "Moderate Activity: Regular physical activity like walking, light jogging, or light cycling. Some exercise 3-5 days a week."
        }

        highlyActiveButton.setOnClickListener {
            activityCaptionTextView.text = "You selected Highly Active"
            activityDescriptionTextView.text = "Highly Active: Intense physical activity, such as running, weight lifting, or intense sports. Frequent exercise 6-7 days a week."
        }
    }
}
