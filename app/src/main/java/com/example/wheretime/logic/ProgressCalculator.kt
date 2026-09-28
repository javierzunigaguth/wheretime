package com.example.wheretime.logic

import com.example.wheretime.data.Category
import com.example.wheretime.data.Entry
import com.example.wheretime.data.Goal
import com.example.wheretime.data.Subcategory
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

object ProgressCalculator {

    fun getWeekRange(referenceDate: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
        val start = referenceDate.with(DayOfWeek.MONDAY)
        val end = start.plusDays(6)
        return Pair(start, end)
    }

    fun totalMinutes(entries: List<Entry>): Int =
        entries.sumOf { it.totalMinutes }

    fun minutesBySubcategory(entries: List<Entry>): Map<Subcategory, Int> =
        entries.groupBy { it.subcategory }
            .mapValues { (_, entriesForSub) -> entriesForSub.sumOf { it.totalMinutes } }

    fun minutesByCategory(entries: List<Entry>): Map<Category, Int> =
        minutesBySubcategory(entries)
            .entries
            .groupBy { it.key.category }
            .mapValues { (_, subEntries) -> subEntries.sumOf { it.value } }

    fun goalProgressPercent(entries: List<Entry>, goal: Goal?): Int {
        if (goal == null || goal.totalTargetMinutes == 0) return 0

        val loggedMinutes = entries
            .filter { it.subcategory == goal.subcategory }
            .sumOf { it.totalMinutes }

        val percent = (loggedMinutes * 100) / goal.totalTargetMinutes
        return percent.coerceIn(0, 100)
    }

    fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%d:%02d:00", hours, minutes)
    }
}