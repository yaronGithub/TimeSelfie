package com.example.timeselfie.utils.date

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date operations.
 */
object DateUtils {

    private fun getDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun getDisplayDateFormat() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private fun getMonthYearFormat() = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    /**
     * Get today's date in the standard format (yyyy-MM-dd).
     */
    fun getTodayString(): String {
        return getDateFormat().format(Date())
    }

    /**
     * Format a date string for display.
     */
    fun formatForDisplay(dateString: String): String {
        return try {
            val date = getDateFormat().parse(dateString)
            getDisplayDateFormat().format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Get the current month and year for capsule naming.
     */
    fun getCurrentMonthYear(): String {
        return getMonthYearFormat().format(Date())
    }
    
    /**
     * Check if a date string represents today.
     */
    fun isToday(dateString: String): Boolean {
        return dateString == getTodayString()
    }
    
    /**
     * Get a list of dates for the next 30 days starting from today.
     */
    fun getNext30Days(): List<String> {
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val formatter = getDateFormat()

        repeat(30) { day ->
            dates.add(formatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    /**
     * Calculate the day number (1-30) for a given date within a 30-day period.
     */
    fun calculateDayNumber(startDate: String, currentDate: String): Int {
        return try {
            val formatter = getDateFormat()
            val start = formatter.parse(startDate)!!
            val current = formatter.parse(currentDate)!!
            val diffInMillis = current.time - start.time
            val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            (diffInDays + 1).coerceIn(1, 30)
        } catch (e: Exception) {
            1
        }
    }
}
