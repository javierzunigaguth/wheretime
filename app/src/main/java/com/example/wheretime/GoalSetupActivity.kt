package com.example.wheretime

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.wheretime.data.AppDatabase
import com.example.wheretime.data.Goal
import com.example.wheretime.data.Subcategory
import kotlinx.coroutines.launch

class GoalSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_goal_setup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val subcategorySpinner: Spinner = findViewById(R.id.subcategorySpinner)
        val subcategoryOptions = mutableListOf("Sub Category")
        subcategoryOptions.addAll(Subcategory.all().map { it.displayName })
        val subcategoryAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, subcategoryOptions) {
            override fun isEnabled(position: Int) = position != 0
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(if (position == 0) android.graphics.Color.GRAY else android.graphics.Color.BLACK)
                return view
            }
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(if (position == 0) android.graphics.Color.GRAY else android.graphics.Color.BLACK)
                return view
            }
        }
        subcategorySpinner.adapter = subcategoryAdapter

        val subcategoryError: TextView = findViewById(R.id.subcategoryError)
        val durationError: TextView = findViewById(R.id.durationError)
        val hoursInput: EditText = findViewById(R.id.hoursInput)
        val setGoalButton: Button = findViewById(R.id.setGoalButton)

        setGoalButton.setOnClickListener {
            subcategoryError.visibility = TextView.GONE
            durationError.visibility = TextView.GONE

            var isValid = true

            if (subcategorySpinner.selectedItemPosition == 0) {
                subcategoryError.text = "Please select a subcategory"
                subcategoryError.visibility = TextView.VISIBLE
                isValid = false
            }

            val hours = hoursInput.text.toString().toIntOrNull() ?: 0

            when {
                hours > 24 * 7 -> {
                    durationError.text = "Weekly hours cannot exceed 168 (24h × 7 days)"
                    durationError.visibility = TextView.VISIBLE
                    isValid = false
                }
                hours == 0 -> {
                    durationError.text = "Please enter a target greater than 0"
                    durationError.visibility = TextView.VISIBLE
                    isValid = false
                }
            }

            if (isValid) {
                val selectedSubcategory = Subcategory.all()[subcategorySpinner.selectedItemPosition - 1]

                val goal = Goal(
                    subcategory = selectedSubcategory,
                    targetHours = hours,
                    targetMinutes = 0
                )

                val repository = AppDatabase.getInstance(applicationContext).goalRepository()

                lifecycleScope.launch {
                    repository.setGoal(goal)
                    finish()
                }
            }
        }
    }
}