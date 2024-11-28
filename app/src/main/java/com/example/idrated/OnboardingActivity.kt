package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var waterFillView: View  // The view representing the water filling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Initialize the water fill view
        waterFillView = findViewById(R.id.waterFillView)

        // Set up ViewPager2 and its adapter
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = OnboardingAdapter(this)

        // Skip text setup
        val skipText: TextView = findViewById(R.id.skipText)
        skipText.setOnClickListener {
            navigateToGoalActivity()  // Go to GoalActivity when clicked
        }

        // Set up Next button
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < 3) {  // If it's not the last page
                viewPager.currentItem = currentItem + 1  // Move to the next page
                animateWaterFill(currentItem + 1)  // Animate the water filling effect
            } else {
                navigateToGoalActivity()  // If it's the last page, navigate to GoalActivity
            }
        }
    }

    // Function to animate the water filling effect
    private var currentHeight = 0f  // Track the current height of the water fill

    private fun animateWaterFill(progress: Int) {
        // Calculate the target height based on the progress (target height in dp)
        val targetHeight = (progress * 200).toFloat()  // Adjust multiplier to control the height

        // Create an animator to animate the height change
        val animator = ValueAnimator.ofFloat(currentHeight, targetHeight)
        animator.duration = 1000  // Duration of 1 second for smooth animation
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            val layoutParams = waterFillView.layoutParams
            layoutParams.height = value.toInt()  // Update the height of the water view
            waterFillView.layoutParams = layoutParams
        }
        animator.start()

        currentHeight = targetHeight  // Update current height after animation
    }

    private fun navigateToGoalActivity() {
        val intent = Intent(this, GoalActivity::class.java)
        startActivity(intent)
        finish()  // Finish OnboardingActivity so the user can't go back
    }
}
