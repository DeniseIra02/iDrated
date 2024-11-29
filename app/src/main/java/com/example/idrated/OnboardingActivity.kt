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

    private lateinit var waterFillView: View
    private lateinit var nextButton: Button
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Initialize the water fill view
        waterFillView = findViewById(R.id.waterFillView)

        // Set up ViewPager2 and its adapter
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = OnboardingAdapter(this)

        // Disable swipe gestures
        viewPager.isUserInputEnabled = false

        // Set up Next button
        nextButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < 3) {
                viewPager.currentItem = currentItem + 1
                animateWaterFill(currentItem + 1)
                updateButtonText(currentItem + 1)
            } else {
                navigateToGoalActivity()
            }
        }
    }

    // Function to animate the water filling effect
    private var currentHeight = 0f

    private fun animateWaterFill(progress: Int) {
        val targetHeight = (progress * 200).toFloat()
        val animator = ValueAnimator.ofFloat(currentHeight, targetHeight)
        animator.duration = 1000
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            val layoutParams = waterFillView.layoutParams
            layoutParams.height = value.toInt()
            waterFillView.layoutParams = layoutParams
        }
        animator.start()
        currentHeight = targetHeight
    }

    private fun updateButtonText(currentPage: Int) {
        if (currentPage == 3) {
            nextButton.text = "Get Started"
        } else {
            nextButton.text = "Next"
        }
    }

    private fun navigateToGoalActivity() {
        val intent = Intent(this, GoalActivity::class.java)
        startActivity(intent)
        finish()
    }
}
