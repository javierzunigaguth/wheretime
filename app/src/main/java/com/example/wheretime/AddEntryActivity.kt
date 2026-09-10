package com.example.wheretime

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wheretime.data.Category
import com.example.wheretime.data.Subcategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.example.wheretime.data.AppDatabase
import com.example.wheretime.data.Entry
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddEntryActivity : AppCompatActivity() {

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_entry)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)
        val categoryOptions = mutableListOf("Category")
        categoryOptions.addAll(Category.values().map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } })
        val categoryAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions) {
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
        categorySpinner.adapter = categoryAdapter

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

        val dateField: TextView = findViewById(R.id.dateField)
        val calendar = Calendar.getInstance()
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        updateDateField(dateField)

        dateField.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = dayOfMonth
                    updateDateField(dateField)
                },
                selectedYear,
                selectedMonth,
                selectedDay
            ).show()
        }

        val categoryError: TextView = findViewById(R.id.categoryError)
        val subcategoryError: TextView = findViewById(R.id.subcategoryError)
        val dateError: TextView = findViewById(R.id.dateError)
        val durationError: TextView = findViewById(R.id.durationError)
        val hoursInput: android.widget.EditText = findViewById(R.id.hoursInput)
        val minutesInput: android.widget.EditText = findViewById(R.id.minutesInput)
        val addEntryButton: Button = findViewById(R.id.addEntryButton)

        addEntryButton.setOnClickListener {
            categoryError.visibility = TextView.GONE
            subcategoryError.visibility = TextView.GONE
            dateError.visibility = TextView.GONE
            durationError.visibility = TextView.GONE

            var isValid = true

            if (categorySpinner.selectedItemPosition == 0) {
                categoryError.text = "Please select a category"
                categoryError.visibility = TextView.VISIBLE
                isValid = false
            }

            if (subcategorySpinner.selectedItemPosition == 0) {
                subcategoryError.text = "Please select a subcategory"
                subcategoryError.visibility = TextView.VISIBLE
                isValid = false
            }

            val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            if (selectedDate.isAfter(LocalDate.now())) {
                dateError.text = "Date cannot be in the future"
                dateError.visibility = TextView.VISIBLE
                isValid = false
            }

            val hours = hoursInput.text.toString().toIntOrNull() ?: 0
            val minutes = minutesInput.text.toString().toIntOrNull() ?: 0

            when {
                hours > 24 -> {
                    durationError.text = "Hours cannot be greater than 24"
                    durationError.visibility = TextView.VISIBLE
                    isValid = false
                }
                minutes >= 60 -> {
                    durationError.text = "Minutes cannot be 60 or more"
                    durationError.visibility = TextView.VISIBLE
                    isValid = false
                }
                (hours * 60 + minutes) > 24 * 60 -> {
                    durationError.text = "Total time cannot exceed 24 hours"
                    durationError.visibility = TextView.VISIBLE
                    isValid = false
                }
                hours == 0 && minutes == 0 -> {
                    durationError.text = "Please enter a duration greater than 0"
                    durationError.visibility = TextView.VISIBLE
                    isValid = false
                }
            }

            if (isValid) {
                val selectedSubcategory = Subcategory.all()[subcategorySpinner.selectedItemPosition - 1]

                val entry = Entry(
                    subcategory = selectedSubcategory,
                    date = selectedDate,
                    hours = hours,
                    minutes = minutes
                )

                val repository = AppDatabase.getInstance(applicationContext).entryRepository()

                lifecycleScope.launch {
                    repository.insert(entry)
                    finish()
                }
            }
        }
    }

    private fun updateDateField(dateField: TextView) {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay)
        val formatter = SimpleDateFormat("EEEE d, yyyy", Locale.getDefault())
        dateField.text = formatter.format(calendar.time)
    }
}