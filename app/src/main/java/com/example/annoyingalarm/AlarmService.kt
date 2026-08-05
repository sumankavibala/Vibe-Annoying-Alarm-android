package com.example.annoyingalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP_ALARM") {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        } else if (action == "REPOST_NOTIFICATION") {
            if (isAlarmRinging) {
                showRingingNotification()
            }
            return START_STICKY
        }

        activeAlarmId = intent?.getStringExtra("ALARM_ID") ?: ""
        activeAlarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm Ringing"
        activeSnoozeMinutes = intent?.getIntExtra("ALARM_SNOOZE_MINUTES", 5) ?: 5
        activeTapCount = intent?.getIntExtra("ALARM_TAP_COUNT", 5) ?: 5
        isAlarmRinging = true

        createNotificationChannel()
        showRingingNotification()

        startRingingAndVibrating()

        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_ID", activeAlarmId)
            putExtra("ALARM_LABEL", activeAlarmLabel)
            putExtra("ALARM_SNOOZE_MINUTES", activeSnoozeMinutes)
            putExtra("ALARM_TAP_COUNT", activeTapCount)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(fullScreenIntent)

        return START_STICKY
    }

    private fun showRingingNotification() {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_ID", activeAlarmId)
            putExtra("ALARM_LABEL", activeAlarmLabel)
            putExtra("ALARM_SNOOZE_MINUTES", activeSnoozeMinutes)
            putExtra("ALARM_TAP_COUNT", activeTapCount)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // DeleteIntent: If notification is swiped, re-post immediately so user never loses control
        val deleteIntent = Intent(this, AlarmService::class.java).apply {
            action = "REPOST_NOTIFICATION"
        }
        val deletePendingIntent = PendingIntent.getService(
            this,
            1,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ALARM RINGING!")
            .setContentText(activeAlarmLabel)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "OPEN ALARM", fullScreenPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startRingingAndVibrating() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        isAlarmRinging = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Annoying Alarm Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification for active ringing alarm"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "annoying_alarm_channel"
        const val NOTIFICATION_ID = 1001

        var isAlarmRinging: Boolean = false
        var activeAlarmId: String = ""
        var activeAlarmLabel: String = ""
        var activeSnoozeMinutes: Int = 5
        var activeTapCount: Int = 5
    }
}
