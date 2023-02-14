package com.rokoblak.chatbackup.util

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


fun Instant.atLocal(): ZonedDateTime = atZone(ZoneId.systemDefault())

fun Instant.formatRelative(): String {
    return relativeTimeAgoFormatted(this)
}

fun Instant.formatDateOnly(): String {
    val format = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
    return atLocal().format(format)
}

private fun relativeTimeAgoFormatted(
    dateTimeInstant: Instant,
    nowInstant: Instant = Instant.now()
): String {
    val dateTime = dateTimeInstant.atLocal()
    val now = nowInstant.atLocal()
    if (dateTime.isAfter(now)) return "just now"
    if (now.year == dateTime.year) {
        val diff = Duration.between(dateTime, now)
        val diffInDays = diff.toDays()
        val diffInHours = diff.toHours()
        val diffInSeconds = diff.seconds
        val diffInMins = diff.toMinutes().mod(60).toLong()

        val pluralSuffix: (value: Long) -> String = { value ->
            if (value > 1) "s" else ""
        }
        return when {
            now.dayOfYear - dateTime.dayOfYear > 1 -> {
                val format = DateTimeFormatter.ofPattern("MMM d 'at' hh:mm", Locale.US)
                format.format(dateTime)
            }
            diffInDays >= 2 -> "$diffInDays day${pluralSuffix(diffInDays)} ago"
            diffInHours >= 1 -> "$diffInHours hour${pluralSuffix(diffInHours)} ago"
            diffInMins >= 1 -> "$diffInMins min${pluralSuffix(diffInMins)} ago"
            else -> "$diffInSeconds seconds ago"
        }
    } else {
        val format = DateTimeFormatter.ofPattern("MMM d yyyy 'at' hh:mm", Locale.US)
        return format.format(dateTime)
    }
}