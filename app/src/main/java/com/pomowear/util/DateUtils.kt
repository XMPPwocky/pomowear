package com.pomowear.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
