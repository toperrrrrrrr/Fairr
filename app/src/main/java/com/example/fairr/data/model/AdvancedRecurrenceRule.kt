package com.example.fairr.data.model

import com.google.firebase.Timestamp
import java.util.*

/**
 * Advanced recurrence rule that supports more complex patterns
 */
data class AdvancedRecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val interval: Int = 1,
    val endDate: Timestamp? = null,
    val startDate: Timestamp = Timestamp.now(),
    // Advanced options
    val dayOfWeek: Int? = null, // Calendar.DAY_OF_WEEK (1=Sunday, 2=Monday, etc.)
    val dayOfMonth: Int? = null, // Day of month (1-31)
    val weekOfMonth: Int? = null, // Week of month (1-5, -1 for last week)
    val monthOfYear: Int? = null, // Month of year (1-12)
    val exceptions: List<Timestamp> = emptyList(), // Dates to skip
    val maxOccurrences: Int? = null // Maximum number of occurrences
) {
    
    /**
     * Calculate the next occurrence date from a given date
     */
    fun getNextOccurrence(fromDate: Date): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = fromDate
        
        // If we have an end date and we're past it, return null
        endDate?.let { end ->
            if (calendar.time.after(end.toDate())) {
                return null
            }
        }
        
        // Calculate next occurrence based on frequency and advanced options
        return when (frequency) {
            RecurrenceFrequency.DAILY -> calculateNextDaily(calendar)
            RecurrenceFrequency.WEEKLY -> calculateNextWeekly(calendar)
            RecurrenceFrequency.MONTHLY -> calculateNextMonthly(calendar)
            RecurrenceFrequency.YEARLY -> calculateNextYearly(calendar)
            else -> null
        }
    }
    
    /**
     * Calculate all occurrences between two dates
     */
    fun getOccurrencesBetween(startDate: Date, endDate: Date): List<Date> {
        val occurrences = mutableListOf<Date>()
        var currentDate = startDate
        var occurrenceCount = 0
        
        while (currentDate.before(endDate) && 
               (maxOccurrences == null || occurrenceCount < maxOccurrences)) {
            
            val nextOccurrence = getNextOccurrence(currentDate)
            if (nextOccurrence == null || nextOccurrence.after(endDate)) {
                break
            }
            
            // Check if this date is in exceptions
            if (!exceptions.any { it.toDate() == nextOccurrence }) {
                occurrences.add(nextOccurrence)
                occurrenceCount++
            }
            
            currentDate = nextOccurrence
        }
        
        return occurrences
    }
    
    private fun calculateNextDaily(calendar: Calendar): Date {
        calendar.add(Calendar.DAY_OF_MONTH, interval)
        return calendar.time
    }
    
    private fun calculateNextWeekly(calendar: Calendar): Date {
        val targetDayOfWeek = dayOfWeek
        if (targetDayOfWeek != null) {
            // Specific day of week (e.g., every Monday)
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            if (currentDayOfWeek == targetDayOfWeek) {
                // If we're already on the target day, move to next week
                calendar.add(Calendar.WEEK_OF_YEAR, interval)
            } else {
                // Calculate days until next target day
                var daysToAdd = targetDayOfWeek - currentDayOfWeek
                if (daysToAdd <= 0) {
                    daysToAdd += 7
                }
                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
            }
        } else {
            // Regular weekly (same day of week)
            calendar.add(Calendar.WEEK_OF_YEAR, interval)
        }
        
        return calendar.time
    }
    
    private fun calculateNextMonthly(calendar: Calendar): Date {
        val targetDay = dayOfMonth
        if (targetDay != null) {
            // Specific day of month (e.g., monthly on the 15th)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            
            if (currentDay == targetDay) {
                // If we're already on the target day, move to next month
                calendar.add(Calendar.MONTH, interval)
            } else {
                // Set to target day of current month
                calendar.set(Calendar.DAY_OF_MONTH, targetDay)
                
                // If we've passed this day this month, move to next month
                if (calendar.time.before(Date())) {
                    calendar.add(Calendar.MONTH, interval)
                }
            }
        } else {
            val targetWeek = weekOfMonth
            val targetDayOfWeek = dayOfWeek
            if (targetWeek != null && targetDayOfWeek != null) {
                // Specific week and day of month (e.g., first Monday of month)
            
            // Move to next month
            calendar.add(Calendar.MONTH, interval)
            
            // Set to first day of month
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            
                // Find the target day of the target week
                while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                // Add weeks to get to target week
                if (targetWeek > 1) {
                    calendar.add(Calendar.WEEK_OF_MONTH, targetWeek - 1)
                } else if (targetWeek == -1) {
                    // Last week: move to next month and go back
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    
                    while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                    }
                }
            } else {
                // Regular monthly (same day of month)
                calendar.add(Calendar.MONTH, interval)
            }
        }
        
        return calendar.time
    }
    
    private fun calculateNextYearly(calendar: Calendar): Date {
        val targetMonth = monthOfYear
        val targetDay = dayOfMonth
        if (targetMonth != null && targetDay != null) {
            // Specific month and day (e.g., yearly on March 15th)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            
            if (currentMonth == targetMonth && calendar.get(Calendar.DAY_OF_MONTH) == targetDay) {
                // If we're already on the target date, move to next year
                calendar.add(Calendar.YEAR, interval)
            } else {
                // Set to target month and day
                calendar.set(Calendar.MONTH, targetMonth - 1) // Convert to 0-based
                calendar.set(Calendar.DAY_OF_MONTH, targetDay)
                
                // If we've passed this date this year, move to next year
                if (calendar.time.before(Date())) {
                    calendar.add(Calendar.YEAR, interval)
                }
            }
        } else {
            // Regular yearly (same month and day)
            calendar.add(Calendar.YEAR, interval)
        }
        
        return calendar.time
    }
    
    /**
     * Convert to display string
     */
    fun toDisplayString(): String {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> {
                if (interval == 1) "Daily" else "Every $interval days"
            }
            RecurrenceFrequency.WEEKLY -> {
                if (dayOfWeek != null) {
                    val dayName = getDayName(dayOfWeek)
                    if (interval == 1) "Every $dayName" else "Every $interval weeks on $dayName"
                } else {
                    if (interval == 1) "Weekly" else "Every $interval weeks"
                }
            }
            RecurrenceFrequency.MONTHLY -> {
                when {
                    dayOfMonth != null -> {
                        val suffix = getDaySuffix(dayOfMonth)
                        if (interval == 1) "Monthly on the ${dayOfMonth}$suffix" 
                        else "Every $interval months on the ${dayOfMonth}$suffix"
                    }
                    weekOfMonth != null && dayOfWeek != null -> {
                        val weekName = when (weekOfMonth) {
                            1 -> "first"
                            2 -> "second"
                            3 -> "third"
                            4 -> "fourth"
                            -1 -> "last"
                            else -> "${weekOfMonth}th"
                        }
                        val dayName = getDayName(dayOfWeek)
                        if (interval == 1) "Monthly on the $weekName $dayName"
                        else "Every $interval months on the $weekName $dayName"
                    }
                    else -> {
                        if (interval == 1) "Monthly" else "Every $interval months"
                    }
                }
            }
            RecurrenceFrequency.YEARLY -> {
                if (monthOfYear != null && dayOfMonth != null) {
                    val monthName = getMonthName(monthOfYear)
                    val suffix = getDaySuffix(dayOfMonth)
                    if (interval == 1) "Yearly on $monthName ${dayOfMonth}$suffix"
                    else "Every $interval years on $monthName ${dayOfMonth}$suffix"
                } else {
                    if (interval == 1) "Yearly" else "Every $interval years"
                }
            }
            else -> "None"
        }
    }
    
    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }
    
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }
    
    private fun getDaySuffix(day: Int): String {
        return when (day) {
            1, 21, 31 -> "st"
            2, 22 -> "nd"
            3, 23 -> "rd"
            else -> "th"
        }
    }
} 