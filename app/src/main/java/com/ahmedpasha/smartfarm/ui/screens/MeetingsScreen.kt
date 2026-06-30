package com.ahmedpasha.smartfarm.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahmedpasha.smartfarm.data.models.Meeting
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import com.ahmedpasha.smartfarm.util.DateHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(viewModel: FarmViewModel) {
    val meetings by viewModel.meetings.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMeeting by remember { mutableStateOf<Meeting?>(null) }
    var showMeetingDetail by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("📝 الاجتماعات والمحاضر", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        if (meetings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Text("لا توجد اجتماعات مسجلة بعد", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(meetings) { meeting ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(12.dp), onClick = { selectedMeeting = meeting; showMeetingDetail = true }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { viewModel.deleteMeeting(meeting) }) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red) }
                            }
                            Text("التاريخ: ${DateHelper.formatForDisplay(meeting.date)}", style = MaterialTheme.typography.bodySmall)
                            Text("المكان: ${meeting.location}", style = MaterialTheme.typography.bodySmall)
                            Text("الحضور: ${meeting.attendees}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.padding(16.dp).align(Alignment.End), containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, contentDescription = "إضافة اجتماع")
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }; var location by remember { mutableStateOf("") }
        var attendees by remember { mutableStateOf("") }; var agenda by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }; var titleError by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showAddDialog = false }, title = { Text("إضافة اجتماع جديد") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it; titleError = false }, label = { Text("العنوان") }, isError = titleError)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("المكان") })
                OutlinedTextField(value = attendees, onValueChange = { attendees = it }, label = { Text("الحضور") })
                OutlinedTextField(value = agenda, onValueChange = { agenda = it }, label = { Text("جدول الأعمال") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("الملاحظات") }, maxLines = 3)
            }},
            confirmButton = { Button(onClick = { if (title.isBlank()) { titleError = true } else { viewModel.insertMeeting(Meeting(title = title, date = DateHelper.getCurrentDate(), location = location, attendees = attendees, agenda = agenda, notes = notes)); showAddDialog = false } }) { Text("حفظ") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") } }
        )
    }

    if (showMeetingDetail && selectedMeeting != null) {
        var isPlaying by remember { mutableStateOf(false) }
        var currentTime by remember { mutableFloatStateOf(0f) }
        val totalDuration = 225f
        LaunchedEffect(isPlaying) { while (isPlaying && currentTime < totalDuration) { delay(100); currentTime = (currentTime + 0.1f).coerceAtMost(totalDuration); if (currentTime >= totalDuration) isPlaying = false } }
        AlertDialog(onDismissRequest = { showMeetingDetail = false }, title = { Text(selectedMeeting!!.title) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("التاريخ: ${DateHelper.formatForDisplay(selectedMeeting!!.date)}")
                Text("المكان: ${selectedMeeting!!.location}")
                Text("الحضور: ${selectedMeeting!!.attendees}")
                Text("جدول الأعمال: ${selectedMeeting!!.agenda}")
                Text("الملاحظات: ${selectedMeeting!!.notes}")
                Divider()
                Text("🎙️ التسجيل الصوتي:", style = MaterialTheme.typography.titleSmall)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(progress = { currentTime / totalDuration }, modifier = Modifier.fillMaxWidth())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatTime(currentTime.toInt())); Text(formatTime(totalDuration.toInt()))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { }) { Icon(Icons.Default.FastRewind, contentDescription = "ترجيع") }
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                                IconButton(onClick = { isPlaying = !isPlaying }) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "إيقاف" else "تشغيل", tint = Color.White) }
                            }
                            IconButton(onClick = { }) { Icon(Icons.Default.FastForward, contentDescription = "تقديم") }
                        }
                    }
                }
            }},
            confirmButton = { TextButton(onClick = { showMeetingDetail = false }) { Text("إغلاق") } }
        )
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60; val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}