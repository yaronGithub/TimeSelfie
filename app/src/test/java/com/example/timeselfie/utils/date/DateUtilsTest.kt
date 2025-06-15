package com.example.timeselfie.utils.date

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class DateUtilsTest {

    @Test
    fun `getTodayString should return current date in correct format`() {
        // When
        val currentDate = DateUtils.getTodayString()

        // Then
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expectedDate = dateFormat.format(Date())
        assertEquals(expectedDate, currentDate)
    }

    @Test
    fun `formatForDisplay should format date correctly`() {
        // Given
        val date = "2025-06-15"

        // When
        val formattedDate = DateUtils.formatForDisplay(date)

        // Then
        assertEquals("Jun 15, 2025", formattedDate)
    }

    @Test
    fun `formatForDisplay should handle invalid date gracefully`() {
        // Given
        val invalidDate = "invalid-date"

        // When
        val formattedDate = DateUtils.formatForDisplay(invalidDate)

        // Then
        assertEquals(invalidDate, formattedDate) // Should return original string
    }

    @Test
    fun `isToday should return true for today's date`() {
        // Given
        val today = DateUtils.getTodayString()

        // When
        val result = DateUtils.isToday(today)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isToday should return false for yesterday's date`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val yesterday = dateFormat.format(calendar.time)

        // When
        val result = DateUtils.isToday(yesterday)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isToday should return false for tomorrow's date`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tomorrow = dateFormat.format(calendar.time)

        // When
        val result = DateUtils.isToday(tomorrow)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getNext30Days should return 30 consecutive dates starting from today`() {
        // When
        val next30Days = DateUtils.getNext30Days()

        // Then
        assertEquals(30, next30Days.size)

        // Check that first date is today
        assertEquals(DateUtils.getTodayString(), next30Days.first())

        // Check that dates are consecutive
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in next30Days.indices) {
            val expectedDate = dateFormat.format(calendar.time)
            assertEquals(expectedDate, next30Days[i])
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    @Test
    fun `getCurrentMonthYear should return current month and year`() {
        // When
        val monthYear = DateUtils.getCurrentMonthYear()

        // Then
        val expectedFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val expectedMonthYear = expectedFormat.format(Date())
        assertEquals(expectedMonthYear, monthYear)
    }

    @Test
    fun `calculateDayNumber should calculate correct day number`() {
        // Given
        val startDate = "2025-06-01"
        val currentDate = "2025-06-15"

        // When
        val dayNumber = DateUtils.calculateDayNumber(startDate, currentDate)

        // Then
        assertEquals(15, dayNumber) // 15th day from June 1st
    }

    @Test
    fun `calculateDayNumber should return 1 for start date`() {
        // Given
        val startDate = "2025-06-01"
        val currentDate = "2025-06-01"

        // When
        val dayNumber = DateUtils.calculateDayNumber(startDate, currentDate)

        // Then
        assertEquals(1, dayNumber)
    }

    @Test
    fun `calculateDayNumber should cap at 30 for dates beyond 30 days`() {
        // Given
        val startDate = "2025-06-01"
        val currentDate = "2025-07-15" // 44 days later

        // When
        val dayNumber = DateUtils.calculateDayNumber(startDate, currentDate)

        // Then
        assertEquals(30, dayNumber) // Should be capped at 30
    }

    @Test
    fun `calculateDayNumber should return 1 for invalid dates`() {
        // Given
        val startDate = "invalid-date"
        val currentDate = "2025-06-15"

        // When
        val dayNumber = DateUtils.calculateDayNumber(startDate, currentDate)

        // Then
        assertEquals(1, dayNumber) // Should return 1 for invalid dates
    }
}
