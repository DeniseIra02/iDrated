package com.example.idrated

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 4 // Four onboarding steps
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AgeInputFragment()
            1 -> GenderInputFragment()
            2 -> ActivityLevelFragment()
            else -> HydrationTipFragment() // Last step with hydration tip
        }
    }
}

