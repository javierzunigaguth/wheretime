package com.example.wheretime

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dateText: TextView = findViewById(R.id.dateText)
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        dateText.text = formatter.format(java.util.Date())

        val pieChart: PieChart = findViewById(R.id.timeChart)

        val entries = listOf(
            PieEntry(18f, "Studying"),
            PieEntry(16f, "Working"),
            PieEntry(8f, "Sports"),
            PieEntry(21f, "Cooking"),
            PieEntry(7f, "Watching TV")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#A8D5BA"),
            android.graphics.Color.parseColor("#F4A6A6"),
            android.graphics.Color.parseColor("#F7D9A0"),
            android.graphics.Color.parseColor("#A9D6E5"),
            android.graphics.Color.parseColor("#D8BFD8")
        )
        dataSet.valueTextSize = 12f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.centerText = "70:00:00\nHours Logged"
        pieChart.setCenterTextSize(14f)
        pieChart.invalidate()
    }
}