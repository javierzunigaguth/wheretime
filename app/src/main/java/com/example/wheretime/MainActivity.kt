package com.example.wheretime

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.wheretime.data.Goal
import com.example.wheretime.data.Subcategory
import androidx.core.graphics.toColorInt
import com.example.wheretime.logic.ProgressCalculator
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
import kotlinx.coroutines.flow.combine

class MainActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private val subcategoryColors = mapOf(
        Subcategory.STUDYING to "#A8D5BA".toColorInt(),
        Subcategory.WORKING to "#F4A6A6".toColorInt(),
        Subcategory.SPORTS to "#F7D9A0".toColorInt(),
        Subcategory.COOKING to "#A9D6E5".toColorInt(),
        Subcategory.WATCHING_TV to "#D8BFD8".toColorInt()
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

        val chartCenterButton: View = findViewById(R.id.chartCenterButton)
        chartCenterButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        val goalSectionButton: View = findViewById(R.id.goalSectionButton)
        goalSectionButton.setOnClickListener {
            startActivity(Intent(this, GoalSetupActivity::class.java))
        }

        val dateText: TextView = findViewById(R.id.dateText)
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        dateText.text = formatter.format(java.util.Date())

        pieChart = findViewById(R.id.timeChart)

        val addEntryButton: Button = findViewById(R.id.addEntryButton)
        addEntryButton.setOnClickListener {
            startActivity(Intent(this, AddEntryActivity::class.java))
        }

        val entryRepository = AppDatabase.getInstance(applicationContext).entryRepository()
        val goalRepository = AppDatabase.getInstance(applicationContext).goalRepository()
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)

        val settingsIcon: Button = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val profileIcon: Button = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        lifecycleScope.launch {
            entryRepository.getEntriesBetween(startOfWeek, endOfWeek)
                .combine(goalRepository.getCurrentGoal()) { entries, goal ->
                    Pair(entries, goal)
                }
                .collectLatest { (entries, goal) ->
                    updateHomescreen(entries)
                    updateGoalSection(entries, goal)
                }
        }
    }

    private fun updateHomescreen(entries: List<Entry>) {
        val minutesBySubcategory = ProgressCalculator.minutesBySubcategory(entries)
        val totalMinutes = ProgressCalculator.totalMinutes(entries)

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
            pieChart.centerText = ProgressCalculator.formatDuration(totalMinutes) + "\nHours Logged"
            pieChart.setCenterTextSize(14f)
            pieChart.invalidate()
        }

        val minutesByCategory = ProgressCalculator.minutesByCategory(entries)
        val productiveMinutes = minutesByCategory[Category.PRODUCTIVE] ?: 0
        val unproductiveMinutes = minutesByCategory[Category.UNPRODUCTIVE] ?: 0

        val productiveBox: TextView = findViewById(R.id.productiveBox)
        val unproductiveBox: TextView = findViewById(R.id.unproductiveBox)

        if (totalMinutes == 0) {
            productiveBox.text = "Productive\n0% (0:00:00)"
            unproductiveBox.text = "Unproductive\n0% (0:00:00)"
        } else {
            val productivePercent = (productiveMinutes * 100) / totalMinutes
            val unproductivePercent = (unproductiveMinutes * 100) / totalMinutes
            productiveBox.text = "Productive\n$productivePercent% (${ProgressCalculator.formatDuration(productiveMinutes)})"
            unproductiveBox.text = "Unproductive\n$unproductivePercent% (${ProgressCalculator.formatDuration(unproductiveMinutes)})"
        }

        val studyingItem: TextView = findViewById(R.id.studyingItem)
        val watchingTvItem: TextView = findViewById(R.id.watchingTvItem)
        val sportsItem: TextView = findViewById(R.id.sportsItem)
        val cookingItem: TextView = findViewById(R.id.cookingItem)

        studyingItem.text = "📖 Studying\n(${ProgressCalculator.formatDuration(minutesBySubcategory[Subcategory.STUDYING] ?: 0)})"
        watchingTvItem.text = "📺 Watching TV\n(${ProgressCalculator.formatDuration(minutesBySubcategory[Subcategory.WATCHING_TV] ?: 0)})"
        sportsItem.text = "🏃 Sports\n(${ProgressCalculator.formatDuration(minutesBySubcategory[Subcategory.SPORTS] ?: 0)})"
        cookingItem.text = "🍳 Cooking\n(${ProgressCalculator.formatDuration(minutesBySubcategory[Subcategory.COOKING] ?: 0)})"
    }

    private fun updateGoalSection(entries: List<Entry>, goal: Goal?) {
        val goalSubtitle: TextView = findViewById(R.id.goalSubtitle)
        val goalProgressBar: android.widget.ProgressBar = findViewById(R.id.goalProgressBar)
        val goalPercentText: TextView = findViewById(R.id.goalPercentText)

        if (goal == null) {
            goalSubtitle.text = "No goal set — tap to set one"
            goalProgressBar.progress = 0
            goalPercentText.text = "0%"
            return
        }

        val percent = ProgressCalculator.goalProgressPercent(entries, goal)

        goalSubtitle.text = "${goal.subcategory.displayName} (${ProgressCalculator.formatDuration(goal.totalTargetMinutes)})"
        goalProgressBar.progress = percent
        goalPercentText.text = "$percent%"
    }

}