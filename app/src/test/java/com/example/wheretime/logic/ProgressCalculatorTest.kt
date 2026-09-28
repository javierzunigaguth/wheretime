package com.example.wheretime.logic

import com.example.wheretime.data.Category
import com.example.wheretime.data.Entry
import com.example.wheretime.data.Goal
import com.example.wheretime.data.Subcategory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ProgressCalculatorTest {

    private fun entry(
        subcategory: Subcategory,
        date: LocalDate = LocalDate.of(2026, 7, 15),
        hours: Int = 0,
        minutes: Int = 0
    ) = Entry(subcategory = subcategory, date = date, hours = hours, minutes = minutes)


    @Test
    fun `totalMinutes returns 0 for empty list`() {
        assertEquals(0, ProgressCalculator.totalMinutes(emptyList()))
    }

    @Test
    fun `totalMinutes sums durations across multiple entries`() {
        val entries = listOf(
            entry(Subcategory.STUDYING, hours = 1, minutes = 30),
            entry(Subcategory.SPORTS, hours = 0, minutes = 45)
        )
        assertEquals(135, ProgressCalculator.totalMinutes(entries))
    }

    @Test
    fun `minutesBySubcategory groups and sums entries of the same subcategory`() {
        val entries = listOf(
            entry(Subcategory.STUDYING, hours = 1, minutes = 0),
            entry(Subcategory.STUDYING, hours = 0, minutes = 30),
            entry(Subcategory.SPORTS, hours = 0, minutes = 45)
        )
        val result = ProgressCalculator.minutesBySubcategory(entries)

        assertEquals(90, result[Subcategory.STUDYING])
        assertEquals(45, result[Subcategory.SPORTS])
    }

    @Test
    fun `minutesBySubcategory returns empty map for empty list`() {
        assertEquals(emptyMap<Subcategory, Int>(), ProgressCalculator.minutesBySubcategory(emptyList()))
    }

    @Test
    fun `minutesByCategory correctly splits productive and unproductive time`() {
        val entries = listOf(
            entry(Subcategory.STUDYING, hours = 1, minutes = 0),
            entry(Subcategory.WORKING, hours = 2, minutes = 0),
            entry(Subcategory.WATCHING_TV, hours = 0, minutes = 30)
        )
        val result = ProgressCalculator.minutesByCategory(entries)

        assertEquals(180, result[Category.PRODUCTIVE])
        assertEquals(30, result[Category.UNPRODUCTIVE])
    }

    @Test
    fun `goalProgressPercent returns 0 when goal is null`() {
        val entries = listOf(entry(Subcategory.STUDYING, hours = 5))
        assertEquals(0, ProgressCalculator.goalProgressPercent(entries, null))
    }

    @Test
    fun `goalProgressPercent computes correct percentage`() {
        val entries = listOf(entry(Subcategory.STUDYING, hours = 6))
        val goal = Goal(subcategory = Subcategory.STUDYING, targetHours = 24, targetMinutes = 0)

        assertEquals(25, ProgressCalculator.goalProgressPercent(entries, goal))
    }

    @Test
    fun `goalProgressPercent ignores entries from other subcategories`() {
        val entries = listOf(
            entry(Subcategory.STUDYING, hours = 6),
            entry(Subcategory.SPORTS, hours = 10)
        )
        val goal = Goal(subcategory = Subcategory.STUDYING, targetHours = 24, targetMinutes = 0)

        assertEquals(25, ProgressCalculator.goalProgressPercent(entries, goal))
    }

    @Test
    fun `goalProgressPercent clamps at 100 when logged time exceeds target`() {
        val entries = listOf(entry(Subcategory.STUDYING, hours = 30))
        val goal = Goal(subcategory = Subcategory.STUDYING, targetHours = 10, targetMinutes = 0)

        assertEquals(100, ProgressCalculator.goalProgressPercent(entries, goal))
    }

    @Test
    fun `goalProgressPercent returns 0 for a zero-target goal`() {
        val entries = listOf(entry(Subcategory.STUDYING, hours = 5))
        val goal = Goal(subcategory = Subcategory.STUDYING, targetHours = 0, targetMinutes = 0)

        assertEquals(0, ProgressCalculator.goalProgressPercent(entries, goal))
    }

    @Test
    fun `getWeekRange returns Monday through Sunday for a mid-week date`() {
        val wednesday = LocalDate.of(2026, 7, 15)
        val (start, end) = ProgressCalculator.getWeekRange(wednesday)

        assertEquals(LocalDate.of(2026, 7, 13), start)
        assertEquals(LocalDate.of(2026, 7, 19), end)
    }

    @Test
    fun `getWeekRange handles a reference date that is itself a Sunday`() {
        val sunday = LocalDate.of(2026, 7, 19)
        val (start, end) = ProgressCalculator.getWeekRange(sunday)

        assertEquals(LocalDate.of(2026, 7, 13), start)
        assertEquals(LocalDate.of(2026, 7, 19), end)
    }

    @Test
    fun `getWeekRange handles a reference date that is itself a Monday`() {
        val monday = LocalDate.of(2026, 7, 13)
        val (start, end) = ProgressCalculator.getWeekRange(monday)

        assertEquals(LocalDate.of(2026, 7, 13), start)
        assertEquals(LocalDate.of(2026, 7, 19), end)
    }


    @Test
    fun `formatDuration formats hours and minutes with zero padding`() {
        assertEquals("2:05:00", ProgressCalculator.formatDuration(125))
    }

    @Test
    fun `formatDuration handles zero minutes`() {
        assertEquals("0:00:00", ProgressCalculator.formatDuration(0))
    }
}