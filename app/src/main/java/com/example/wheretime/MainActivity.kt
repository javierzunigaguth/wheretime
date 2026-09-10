package com.example.wheretime

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.wheretime.data.AppDatabase
import com.example.wheretime.data.Category
import com.example.wheretime.data.Entry
import com.example.wheretime.data.Subcategory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private val subcategoryColors = mapOf(
        Subcategory.STUDYING to Color.parseColor("#A8D5BA"),
        Subcategory.WORKING to Color.parseColor("#F4A6A6"),
        Subcategory.SPORTS to Color.parseColor("#F7D9A0"),
        Subcategory.COOKING to Color.parseColor("#A9D6E5"),
        Subcategory.WATCHING_TV to Color.parseColor("#D8BFD8")
    )

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

        pieChart = findViewById(R.id.timeChart)

        val addEntryButton: Button = findViewById(R.id.addEntryButton)
        addEntryButton.setOnClickListener {
            startActivity(Intent(this, AddEntryActivity::class.java))
        }

        val repository = AppDatabase.getInstance(applicationContext).entryRepository()
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)

        lifecycleScope.launch {
            repository.getEntriesBetween(startOfWeek, endOfWeek).collectLatest { entries ->
                updateHomescreen(entries)
            }
        }
    }

    private fun updateHomescreen(entries: List<Entry>) {
        val minutesBySubcategory: Map<Subcategory, Int> = entries
            .groupBy { it.subcategory }
            .mapValues { (_, entriesForSub) -> entriesForSub.sumOf { it.totalMinutes } }

        val totalMinutes = minutesBySubcategory.values.sum()

        if (totalMinutes == 0) {
            pieChart.clear()
            pieChart.centerText = "No entries\nthis week"
            pieChart.invalidate()
        } else {
            val pieEntries = minutesBySubcategory
                .filterValues { it > 0 }
                .map { (subcategory, minutes) -> PieEntry(minutes / 60f, subcategory.displayName) }

            val colors = minutesBySubcategory
                .filterValues { it > 0 }
                .keys
                .map { subcategoryColors.getValue(it) }

            val dataSet = PieDataSet(pieEntries, "")
            dataSet.colors = colors
            dataSet.valueTextSize = 12f

            pieChart.data = PieData(dataSet)
            pieChart.description.isEnabled = false
            pieChart.legend.isEnabled = true
            pieChart.isDrawHoleEnabled = true
            pieChart.holeRadius = 58f
            pieChart.transparentCircleRadius = 61f
            pieChart.centerText = formatDuration(totalMinutes) + "\nHours Logged"
            pieChart.setCenterTextSize(14f)
            pieChart.invalidate()
        }

        val productiveMinutes = minutesBySubcategory
            .filterKeys { it.category == Category.PRODUCTIVE }
            .values.sum()
        val unproductiveMinutes = minutesBySubcategory
            .filterKeys { it.category == Category.UNPRODUCTIVE }
            .values.sum()

        val productiveBox: TextView = findViewById(R.id.productiveBox)
        val unproductiveBox: TextView = findViewById(R.id.unproductiveBox)

        if (totalMinutes == 0) {
            productiveBox.text = "Productive\n0% (0:00:00)"
            unproductiveBox.text = "Unproductive\n0% (0:00:00)"
        } else {
            val productivePercent = (productiveMinutes * 100) / totalMinutes
            val unproductivePercent = (unproductiveMinutes * 100) / totalMinutes
            productiveBox.text = "Productive\n$productivePercent% (${formatDuration(productiveMinutes)})"
            unproductiveBox.text = "Unproductive\n$unproductivePercent% (${formatDuration(unproductiveMinutes)})"
        }

        val studyingItem: TextView = findViewById(R.id.studyingItem)
        val watchingTvItem: TextView = findViewById(R.id.watchingTvItem)
        val sportsItem: TextView = findViewById(R.id.sportsItem)
        val cookingItem: TextView = findViewById(R.id.cookingItem)

        studyingItem.text = "📖 Studying\n(${formatDuration(minutesBySubcategory[Subcategory.STUDYING] ?: 0)})"
        watchingTvItem.text = "📺 Watching TV\n(${formatDuration(minutesBySubcategory[Subcategory.WATCHING_TV] ?: 0)})"
        sportsItem.text = "🏃 Sports\n(${formatDuration(minutesBySubcategory[Subcategory.SPORTS] ?: 0)})"
        cookingItem.text = "🍳 Cooking\n(${formatDuration(minutesBySubcategory[Subcategory.COOKING] ?: 0)})"
    }

    private fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%d:%02d:00", hours, minutes)
    }
}