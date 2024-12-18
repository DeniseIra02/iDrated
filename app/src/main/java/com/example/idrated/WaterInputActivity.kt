package com.example.idrated

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.idrated.R

class WaterInputActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_input)

        // Find views by ID
        val etWaterAmount = findViewById<EditText>(R.id.et_water_amount)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        // Set up button click listener
        btnSubmit.setOnClickListener {
            val inputText = etWaterAmount.text.toString().trim()

            if (inputText.isNotEmpty()) {
                val waterAmount = inputText.toDoubleOrNull()

                if (waterAmount != null) {
                    // Pass water amount to GoalActivity
                    val intent = Intent(this, GoalActivity::class.java)
                    intent.putExtra("waterAmount", waterAmount)  // Pass the value to GoalActivity
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a water amount.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
