package com.ahmedpasha.smartfarm.util

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))

    fun getCurrentDate(): String = dbFormat.format(Date())
    
    fun formatForDisplay(dateString: String): String {
        return try {
            val date = dbFormat.parse(dateString)
            date?.let { displayFormat.format(it) } ?: dateString
        } catch (e: Exception) { dateString }
    }
}