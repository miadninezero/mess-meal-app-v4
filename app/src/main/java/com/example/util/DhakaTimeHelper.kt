package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DhakaTimeHelper {

    val DHAKA_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun getDhakaCalendar(): Calendar {
        return Calendar.getInstance(DHAKA_ZONE)
    }

    fun getTodayDhakaDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = DHAKA_ZONE
        }
        return sdf.format(Date())
    }

    fun isFriday(dateString: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = DHAKA_ZONE
            }
            val date = sdf.parse(dateString) ?: return false
            val cal = Calendar.getInstance(DHAKA_ZONE).apply {
                time = date
            }
            cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks whether the booking deadline has passed for a specific date and meal deadline.
     * @param dateString Target date in "yyyy-MM-dd" format
     * @param deadlineHHmm Deadline time in "HH:mm" (24-hr) in Asia/Dhaka
     * @return true if current Dhaka time is after the deadline for this date
     */
    fun isDeadlinePassed(dateString: String, deadlineHHmm: String): Boolean {
        return try {
            val nowCal = Calendar.getInstance(DHAKA_ZONE)

            val parts = deadlineHHmm.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = DHAKA_ZONE
            }
            val targetDate = sdf.parse(dateString) ?: return true

            val targetCal = Calendar.getInstance(DHAKA_ZONE).apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            nowCal.after(targetCal)
        } catch (_: Exception) {
            true
        }
    }

    fun format12Hour(deadlineHHmm: String): String {
        return try {
            val parts = deadlineHHmm.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
        } catch (_: Exception) {
            deadlineHHmm
        }
    }

    /**
     * Converts a "yyyy-MM-dd" date string into a clean, human-readable format like "10 Nov 2026".
     * If parsing fails, returns the original string gracefully.
     */
    fun formatReadableDate(dateString: String): String {
        if (dateString.isBlank()) return ""
        return try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = DHAKA_ZONE
            }
            val d = sdfIn.parse(dateString.trim()) ?: return dateString
            val sdfOut = SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
                timeZone = DHAKA_ZONE
            }
            sdfOut.format(d)
        } catch (_: Exception) {
            dateString
        }
    }

    fun shiftDate(dateString: String, days: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = DHAKA_ZONE
            }
            val d = sdf.parse(dateString.trim()) ?: return dateString
            val cal = Calendar.getInstance(DHAKA_ZONE).apply {
                time = d
                add(Calendar.DAY_OF_YEAR, days)
            }
            sdf.format(cal.time)
        } catch (_: Exception) {
            dateString
        }
    }
}
