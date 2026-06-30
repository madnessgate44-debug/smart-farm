package com.ahmedpasha.smartfarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahmedpasha.smartfarm.ui.components.CustomChip
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import com.ahmedpasha.smartfarm.util.DateHelper

@Composable
fun WorkerManagement(viewModel: FarmViewModel) {
    val workers by viewModel.workers.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val todayDate = DateHelper.getCurrentDate()
    val todayAttendance = attendance.filter { it.date == todayDate }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("👷 إدارة العمال", style = MaterialTheme.typography.headlineMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.markAllWorkersPresent(todayDate) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("تسجيل الكل حاضر") }
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("إضافة عامل") }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("✅ حاضر", todayAttendance.count { it.status == "حاضر" }.toString(), Color(0xFF10B981))
                StatItem("❌ غائب", todayAttendance.count { it.status == "غائب" }.toString(), Color(0xFFEF4444))
                StatItem("🏖️ إجازة", todayAttendance.count { it.status == "إجازة" }.toString(), Color(0xFF3B82F6))
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(workers) { worker ->
                val workerAttendance = todayAttendance.find { it.workerCode == worker.code }
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(worker.name, style = MaterialTheme.typography.titleSmall)
                            Text(worker.job, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("الأجر: ${worker.dailyRate} ج/يوم", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            CustomChip(text = worker.status, selected = true, onClick = {}, selectedColor = when(worker.status) { "نشط" -> Color(0xFF10B981); "في إجازة" -> Color(0xFFF59E0B); else -> Color(0xFF9CA3AF) })
                            if (workerAttendance != null) {
                                Text(workerAttendance.status, style = MaterialTheme.typography.bodySmall, color = when(workerAttendance.status) { "حاضر" -> Color(0xFF10B981); "غائب" -> Color(0xFFEF4444); else -> Color(0xFF3B82F6) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}