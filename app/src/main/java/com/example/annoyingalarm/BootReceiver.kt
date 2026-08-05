package com.example.annoyingalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val storage = AlarmStorage(context)
            val scheduler = AlarmScheduler(context)
            val alarms = storage.getAlarms()

            for (alarm in alarms) {
                if (alarm.isEnabled) {
                    scheduler.schedule(alarm)
                }
            }
        }
    }
}
