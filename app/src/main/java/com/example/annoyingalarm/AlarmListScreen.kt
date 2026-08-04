package com.example.annoyingalarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

val CherryRed = Color(0xFFDC2626)
val CherryRedLight = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(CherryRed.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Annoying Alarm Splash Logo",
                    tint = CherryRed,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Annoying Alarm",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Easy Snooze.",
                fontSize = 14.sp,
                color = CherryRed,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    alarms: List<AlarmItem>,
    onToggleAlarm: (AlarmItem, Boolean) -> Unit,
    onAddAlarm: (AlarmItem) -> Unit,
    onUpdateAlarm: (AlarmItem) -> Unit,
    onDeleteAlarm: (AlarmItem) -> Unit
) {
    var editingAlarm by remember { mutableStateOf<AlarmItem?>(null) }
    var isAddingNewAlarm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Clock",
                            tint = CherryRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Annoying Alarm",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNewAlarm = true },
                containerColor = CherryRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = Color(0xFF000000)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (alarms.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Alarms Set", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Swipe left or right to delete an alarm", color = Color.Gray.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    onDeleteAlarm(alarm)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> CherryRed
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            },
                            content = {
                                AlarmCard(
                                    alarm = alarm,
                                    onToggle = { isChecked -> onToggleAlarm(alarm, isChecked) },
                                    onClickCard = { editingAlarm = alarm }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (isAddingNewAlarm) {
        val cal = Calendar.getInstance()
        val newAlarm = AlarmItem(
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            isEnabled = true,
            label = "Alarm",
            snoozeMinutes = 5
        )
        AlarmBottomSheet(
            alarm = newAlarm,
            title = "Add Alarm",
            onDismiss = { isAddingNewAlarm = false },
            onSave = { savedAlarm ->
                onAddAlarm(savedAlarm)
                isAddingNewAlarm = false
            }
        )
    }

    editingAlarm?.let { alarmToEdit ->
        AlarmBottomSheet(
            alarm = alarmToEdit,
            title = "Edit Alarm",
            onDismiss = { editingAlarm = null },
            onSave = { updatedAlarm ->
                onUpdateAlarm(updatedAlarm)
                editingAlarm = null
            }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmItem,
    onToggle: (Boolean) -> Unit,
    onClickCard: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) Color(0xFF121212) else Color(0xFF121212).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickCard() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = alarm.getFormattedTime(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) Color.White else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.label,
                        fontSize = 14.sp,
                        color = if (alarm.isEnabled) CherryRed else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${alarm.getRepeatDaysText()} • Snooze ${alarm.snoozeMinutes}m",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF000000),
                    checkedTrackColor = CherryRed,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF262626)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(
    alarm: AlarmItem,
    title: String,
    onDismiss: () -> Unit,
    onSave: (AlarmItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = false
    )
    var isInputModeKeyboard by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf(alarm.label) }
    var repeatDays by remember { mutableStateOf(alarm.repeatDays) }
    var snoozeMinutes by remember { mutableStateOf(alarm.snoozeMinutes) }

    val snoozeOptions = listOf(1, 5, 10, 15, 20, 25, 30)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121212),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Toggle Dial / Keyboard Input Mode
                IconButton(onClick = { isInputModeKeyboard = !isInputModeKeyboard }) {
                    Icon(
                        imageVector = if (isInputModeKeyboard) Icons.Default.Schedule else Icons.Default.Keyboard,
                        contentDescription = "Toggle Time Picker Mode",
                        tint = CherryRed
                    )
                }
            }

            // Clickable Display Area or Input Toggle Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isInputModeKeyboard = !isInputModeKeyboard },
                contentAlignment = Alignment.Center
            ) {
                if (isInputModeKeyboard) {
                    TimeInput(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            timeSelectorSelectedContainerColor = CherryRed.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = Color(0xFF1E1E1E),
                            timeSelectorSelectedContentColor = CherryRed,
                            timeSelectorUnselectedContentColor = Color.White,
                            periodSelectorBorderColor = CherryRed,
                            periodSelectorSelectedContainerColor = CherryRed,
                            periodSelectorUnselectedContainerColor = Color(0xFF1E1E1E),
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.White
                        )
                    )
                } else {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFF1E1E1E),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.White,
                            selectorColor = CherryRed,
                            containerColor = Color(0xFF1E1E1E),
                            periodSelectorBorderColor = CherryRed,
                            periodSelectorSelectedContainerColor = CherryRed,
                            periodSelectorUnselectedContainerColor = Color(0xFF1E1E1E),
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.White,
                            timeSelectorSelectedContainerColor = CherryRed.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = Color(0xFF1E1E1E),
                            timeSelectorSelectedContentColor = CherryRed,
                            timeSelectorUnselectedContentColor = Color.White
                        )
                    )
                }
            }

            // Alarm Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CherryRed,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Repeat Days Selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Repeat Days", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf(
                        1 to "S", 2 to "M", 3 to "T", 4 to "W", 5 to "T", 6 to "F", 7 to "S"
                    )
                    days.forEach { (dayInt, dayLabel) ->
                        val isSelected = repeatDays.contains(dayInt)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) CherryRed else Color(0xFF262626)
                                )
                                .clickable {
                                    repeatDays = if (isSelected) {
                                        repeatDays - dayInt
                                    } else {
                                        repeatDays + dayInt
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Snooze Duration Selector (1, 5, 10, 15, 20, 25, 30 min)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Snooze Duration", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    snoozeOptions.forEach { option ->
                        val isSelected = snoozeMinutes == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) CherryRed else Color(0xFF262626)
                                )
                                .clickable { snoozeMinutes = option },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${option}m",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Save and Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        onSave(
                            alarm.copy(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                label = label,
                                repeatDays = repeatDays,
                                snoozeMinutes = snoozeMinutes
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CherryRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
