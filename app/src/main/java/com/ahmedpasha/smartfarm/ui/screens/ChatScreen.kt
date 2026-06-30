package com.ahmedpasha.smartfarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: FarmViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌾 المزرعة الذكية", style = MaterialTheme.typography.headlineMedium)
                Text("الأستاذ أحمد باشا", style = MaterialTheme.typography.titleMedium)
                Text("المستشار الذكي - الأستاذ بليغ", style = MaterialTheme.typography.bodySmall)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                ) {
                    Text(
                        "الأستاذ أحمد يا باشا، مرحب بيك! نظام إدارة المزرعة شغال وجاهز ✓\nقولي إيه اللي حصل النهارده في المزرعة؟",
                        modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(chatMessages) { (message, isUser) ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = CardDefaults.cardColors(containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                        shape = if (isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                    ) {
                        Text(message, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showVoiceDialog = true }, modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Mic, contentDescription = "تسجيل صوتي", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = inputText, onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f), placeholder = { Text("اكتب أو تحدث هنا...") },
                    shape = RoundedCornerShape(24.dp), singleLine = true
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.addChatMessage(inputText, true)
                        inputText = ""
                        coroutineScope.launch { listState.animateScrollToItem(listState.layoutInfo.totalItemsCount) }
                    }
                }) { Icon(Icons.Default.Send, contentDescription = "إرسال", tint = MaterialTheme.colorScheme.primary) }
            }
        }
    }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { Text("🎙️ التسجيل الصوتي", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(if (isRecording) Color.Red else MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isRecording) {
                        Text("⏺️ جاري التسجيل...", color = Color.Red, style = MaterialTheme.typography.titleMedium)
                        Text("00:12", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    isRecording = !isRecording
                    if (!isRecording) {
                        viewModel.addChatMessage("تسجيل صوتي من الأستاذ أحمد", true)
                        showVoiceDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary)) {
                    Text(if (isRecording) "⏹️ إيقاف" else "▶️ بدء التسجيل")
                }
            },
            dismissButton = { TextButton(onClick = { showVoiceDialog = false }) { Text("إلغاء") } }
        )
    }
}