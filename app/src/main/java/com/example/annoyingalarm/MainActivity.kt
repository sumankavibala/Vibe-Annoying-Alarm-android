package com.example.annoyingalarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var storage: AlarmStorage
    private lateinit var scheduler: AlarmScheduler
    private val alarmList = mutableStateListOf<AlarmItem>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = AlarmStorage(applicationContext)
        scheduler = AlarmScheduler(applicationContext)

        alarmList.clear()
        alarmList.addAll(storage.getAlarms())

        checkPermissions()

        setContent {
            var isSplashVisible by remember { mutableStateOf(true) }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF000000),
                    surface = Color(0xFF121212),
                    primary = Color(0xFFDC2626)
                )
            ) {
                if (isSplashVisible) {
                    SplashScreen(onSplashFinished = { isSplashVisible = false })
                } else {
                    AlarmListScreen(
                        alarms = alarmList,
                        onToggleAlarm = { alarm, isChecked ->
                            val index = alarmList.indexOfFirst { it.id == alarm.id }
                            if (index != -1) {
                                val updated = alarmList[index].copy(isEnabled = isChecked)
                                alarmList[index] = updated
                                storage.saveAlarms(alarmList.toList())

                                if (isChecked) {
                                    scheduler.schedule(updated)
                                } else {
                                    scheduler.cancel(updated)
                                }
                            }
                        },
                        onAddAlarm = { newAlarm ->
                            alarmList.add(newAlarm)
                            storage.saveAlarms(alarmList.toList())
                            scheduler.schedule(newAlarm)
                        },
                        onUpdateAlarm = { updatedAlarm ->
                            val index = alarmList.indexOfFirst { it.id == updatedAlarm.id }
                            if (index != -1) {
                                alarmList[index] = updatedAlarm
                                storage.saveAlarms(alarmList.toList())
                                if (updatedAlarm.isEnabled) {
                                    scheduler.schedule(updatedAlarm)
                                } else {
                                    scheduler.cancel(updatedAlarm)
                                }
                            }
                        },
                        onDeleteAlarm = { alarm ->
                            scheduler.cancel(alarm)
                            alarmList.removeIf { it.id == alarm.id }
                            storage.saveAlarms(alarmList.toList())
                        }
                    )
                }
            }
        }
    }



    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
