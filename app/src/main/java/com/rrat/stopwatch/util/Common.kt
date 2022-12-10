package com.rrat.stopwatch.util


fun formatTime(hours: String, minutes: String, seconds: String): String{
    return "$hours:$minutes:$seconds"
}

fun Int.pad(): String{
    return this.toString().padStart(2, '0')
}