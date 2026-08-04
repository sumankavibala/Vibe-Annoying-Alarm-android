package com.example.annoyingalarm

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class AlarmItem(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "Alarm",
    val repeatDays: Set<Int> = emptySet(), // 1 = Sun, 2 = Mon, ..., 7 = Sat
    val snoozeMinutes: Int = 5,
    val snoozeCount: Int = 0
) {
    fun getFormattedTime(): String {
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        return String.format("%02d:%02d %s", displayHour, minute, amPm)
    }

    fun getRepeatDaysText(): String {
        if (repeatDays.isEmpty()) return "Once"
        if (repeatDays.size == 7) return "Every day"
        val dayNames = arrayOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return repeatDays.sorted().joinToString(", ") { dayNames[it] }
    }
}

class AlarmStorage(context: Context) {
    private val prefs = context.getSharedPreferences("annoying_alarms_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAlarms(): List<AlarmItem> {
        val json = prefs.getString("alarms_list", null) ?: return defaultAlarms()
        val type = object : TypeToken<List<AlarmItem>>() {}.type
        return gson.fromJson(json, type) ?: defaultAlarms()
    }

    fun saveAlarms(alarms: List<AlarmItem>) {
        val json = gson.toJson(alarms)
        prefs.edit().putString("alarms_list", json).apply()
    }

    private fun defaultAlarms(): List<AlarmItem> {
        val defaults = listOf(
            AlarmItem(hour = 7, minute = 0, isEnabled = true, label = "Wake up!", snoozeMinutes = 5),
            AlarmItem(hour = 8, minute = 30, isEnabled = false, label = "Work time", snoozeMinutes = 10)
        )
        saveAlarms(defaults)
        return defaults
    }
}
