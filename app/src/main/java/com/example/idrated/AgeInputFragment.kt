package com.example.idrated

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment

class AgeInputFragment : Fragment(R.layout.fragment_age_input) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ageSeekBar: SeekBar = view.findViewById(R.id.ageSeekBar)
        val ageText: TextView = view.findViewById(R.id.ageText)

        // Resize and tint the thumb dynamically
        val thumbDrawable: Drawable = ageSeekBar.thumb
        val thumbWidth = 1000 // desired width of thumb
        val thumbHeight = 1000 // desired height of thumb

        // Wrap and tint the thumb drawable
        val wrappedDrawable = DrawableCompat.wrap(thumbDrawable)
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        // Set the bounds of the thumb drawable
        wrappedDrawable.setBounds(0, 0, thumbWidth, thumbHeight)

        // Apply the modified thumb back to the SeekBar
        ageSeekBar.thumb = wrappedDrawable

        // Update the displayed age as the SeekBar value changes
        ageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ageText.text = "Age: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
