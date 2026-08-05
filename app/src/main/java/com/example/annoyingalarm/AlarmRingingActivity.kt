package com.example.annoyingalarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.annoyingalarm.ui.components.InterstitialAdManager

import androidx.activity.compose.BackHandler

class AlarmRingingActivity : ComponentActivity() {

    private var currentStep by mutableStateOf("RINGING") // "RINGING" or "TAP_CHALLENGE"
    private var pendingIsSnooze by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Preload Interstitial Ad as soon as alarm starts ringing
        InterstitialAdManager.loadAd(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmId = intent.getStringExtra("ALARM_ID") ?: ""
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Wake Up!"
        val snoozeMinutes = intent.getIntExtra("ALARM_SNOOZE_MINUTES", 5)

        setContent {
            BackHandler(enabled = true) {
                // Prevent hardware back button from closing alarm ringing screen
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF000000),
                    surface = Color(0xFF121212)
                )
            ) {
                if (currentStep == "RINGING") {
                    AlarmRingingScreen(
                        label = alarmLabel,
                        snoozeMinutes = snoozeMinutes,
                        onSnoozeRequested = {
                            pendingIsSnooze = true
                            triggerAdAndGoToTapChallenge()
                        },
                        onStopRequested = {
                            pendingIsSnooze = false
                            triggerAdAndGoToTapChallenge()
                        }
                    )
                } else {
                    TapChallengeScreen(
                        isSnooze = pendingIsSnooze,
                        snoozeMinutes = snoozeMinutes,
                        onChallengeCompleted = {
                            val serviceIntent = Intent(this@AlarmRingingActivity, AlarmService::class.java).apply {
                                action = "STOP_ALARM"
                            }
                            startService(serviceIntent)

                            if (pendingIsSnooze) {
                                AlarmScheduler(applicationContext).scheduleSnooze(alarmId, alarmLabel, snoozeMinutes = snoozeMinutes)
                            }

                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun triggerAdAndGoToTapChallenge() {
        // Transition step to TAP_CHALLENGE immediately so activity content is ready
        currentStep = "TAP_CHALLENGE"

        // Show real AdMob Interstitial Ad on top
        InterstitialAdManager.showAd(this@AlarmRingingActivity) {
            // Ad dismissed callback
        }
    }
}

@Composable
fun AlarmRingingScreen(
    label: String,
    snoozeMinutes: Int,
    onSnoozeRequested: () -> Unit,
    onStopRequested: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF000000), Color(0xFF2A0812), Color(0xFF4C0B1B))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Pulsing Alarm Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626).copy(alpha = 0.2f))
                    .border(2.dp, Color(0xFFDC2626), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Ringing",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFDC2626).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ad Required to Snooze or Stop!",
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons side-by-side (parallel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSnoozeRequested,
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Snooze (${snoozeMinutes}m)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Watch Ad", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                Button(
                    onClick = onStopRequested,
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STOP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Watch Ad", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TapChallengeScreen(
    isSnooze: Boolean,
    snoozeMinutes: Int,
    onChallengeCompleted: () -> Unit
) {
    val tapCountRequired = 5
    var userTaps by remember { mutableStateOf(0) }
    val cherryRed = Color(0xFFDC2626)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSnooze) "Snooze (${snoozeMinutes}m) Verification" else "Stop Verification",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tap the button ${tapCountRequired - userTaps} more time${if (tapCountRequired - userTaps > 1) "s" else ""} to ${if (isSnooze) "Snooze" else "Stop"}!",
                    color = cherryRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(cherryRed)
                        .clickable {
                            if (userTaps + 1 >= tapCountRequired) {
                                onChallengeCompleted()
                            } else {
                                userTaps += 1
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TAP ME!\n(${tapCountRequired - userTaps})",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = "Annoying Alarm Core - Complete taps to dismiss sound",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}
