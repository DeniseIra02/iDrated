package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var nextButton: Button
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Set up ViewPager2 and its adapter
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = OnboardingAdapter(this)

        // Disable swipe gestures (optional)
        viewPager.isUserInputEnabled = false

        // Set up Next button
        nextButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < 3) {
                viewPager.currentItem = currentItem + 1
                updateButtonText(currentItem + 1)
            } else {
                navigateToGoalActivity()
            }
        }
    }

    // Update the button text when we reach the last page
    private fun updateButtonText(currentPage: Int) {
        if (currentPage == 3) {
            nextButton.text = "Get Started"  // Change to "Get Started" on the last page
        } else {
            nextButton.text = ""  // No text (only the icon) on intermediate pages
        }
    }

    // Navigate to the next activity
    private fun navigateToGoalActivity() {
        val intent = Intent(this, GoalActivity::class.java)
        startActivity(intent)
        finish()  // Finish the current activity
    }
}
