package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import com.habitrpg.android.habitica.R
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DateUtils {
    companion object {
        fun createDate(year: Int, month: Int, day: Int): Date {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }
    }
}

fun Date.getAgoString(res: Resources): String {
    return this.time.getAgoString(res)
}

fun Long.getAgoString(res: Resources): String {
    val diff = (Date().time - this).toDuration(DurationUnit.MILLISECONDS)

    val diffMinutes = diff.inWholeMinutes
    val diffHours = diff.inWholeHours
    val diffDays = diff.inWholeDays
    val diffWeeks = diffDays / 7
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L -> if (diffMonths == 1L) {
            res.getString(R.string.ago_1month)
        } else res.getString(R.string.ago_months, diffMonths)
        diffWeeks != 0L -> if (diffWeeks == 1L) {
            res.getString(R.string.ago_1week)
        } else res.getString(R.string.ago_weeks, diffWeeks)
        diffDays != 0L -> if (diffDays == 1L) {
            res.getString(R.string.ago_1day)
        } else res.getString(R.string.ago_days, diffDays)
        diffHours != 0L -> if (diffHours == 1L) {
            res.getString(R.string.ago_1hour)
        } else res.getString(R.string.ago_hours, diffHours)
        diffMinutes == 1L -> res.getString(R.string.ago_1Minute)
        else -> res.getString(R.string.ago_minutes, diffMinutes)
    }
}

fun Date.getRemainingString(res: Resources): String {
    return this.time.getRemainingString(res)
}

fun Long.getRemainingString(res: Resources): String {
    val diff = (this - Date().time).toDuration(DurationUnit.MILLISECONDS)

    val diffMinutes = diff.inWholeMinutes
    val diffHours = diff.inWholeHours
    val diffDays = diff.inWholeDays
    val diffWeeks = diffDays / 7
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L -> if (diffMonths == 1L) {
            res.getString(R.string.remaining_1month)
        } else res.getString(R.string.remaining_months, diffMonths)
        diffWeeks != 0L -> if (diffWeeks == 1L) {
            res.getString(R.string.remaining_1week)
        } else res.getString(R.string.remaining_weeks, diffWeeks)
        diffDays != 0L -> if (diffDays == 1L) {
            res.getString(R.string.remaining_1day)
        } else res.getString(R.string.remaining_days, diffDays)
        diffHours != 0L -> if (diffHours == 1L) {
            res.getString(R.string.remaining_1hour)
        } else res.getString(R.string.remaining_hours, diffHours)
        diffMinutes == 1L -> res.getString(R.string.remaining_1Minute)
        else -> res.getString(R.string.remaining_minutes, diffMinutes)
    }
}

fun Date.getShortRemainingString(): String {
    return time.getShortRemainingString()
}

fun Long.getShortRemainingString(): String {
    var diff = (this - Date().time).toDuration(DurationUnit.MILLISECONDS)

    val diffDays = diff.toInt(DurationUnit.DAYS)
    diff -= diffDays.toDuration(DurationUnit.DAYS)
    val diffHours = diff.toInt(DurationUnit.HOURS)
    diff -= diffHours.toDuration(DurationUnit.HOURS)
    val diffMinutes = diff.toInt(DurationUnit.MINUTES)
    diff -= diffMinutes.toDuration(DurationUnit.MINUTES)
    val diffSeconds = diff.toInt(DurationUnit.SECONDS)

    var str = "${diffMinutes}m"
    if (diffHours > 0) {
        str = "${diffHours}h $str"
    }
    if (diffDays > 0) {
        str = "${diffDays}d $str"
    }
    if (diffDays == 0 && diffHours == 0) {
        str = "$str ${diffSeconds}s"
    }
    return str
}

fun Duration.getMinuteOrSeconds(): DurationUnit {
    return if (this.inWholeHours < 1) DurationUnit.SECONDS else DurationUnit.MINUTES
}
