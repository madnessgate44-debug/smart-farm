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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmedpasha.smartfarm.data.models.FarmTask
import com.ahmedpasha.smartfarm.ui.components.CustomChip
import com.ahmedpasha.smartfarm.ui.theme.*
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import com.ahmedpasha.smartfarm.util.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: FarmViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val crops by viewModel.crops.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("الكل") }
    var priorityFilter by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<FarmTask?>(null) }
    var showProgressDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter { task ->
        (searchQuery.isEmpty() || task.taskName.contains(searchQuery, ignoreCase = true) || task.workerName.contains(searchQuery, ignoreCase = true)) &&
        (statusFilter == "الكل" || task.status == statusFilter) &&
        (priorityFilter == "الكل" || task.priority == priorityFilter)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("📋 جدولة المهام والعمليات", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("📊 إجمالي", tasks.size.toString(), Color(0xFF3B82F6), Modifier.weight(1f))
            StatCard("✅ مكتملة", tasks.count { it.status == "مكتمل" }.toString(), CompletedGreen, Modifier.weight(1f))
            StatCard("⚡ جاري", tasks.count { it.status == "جاري العمل" }.toString(), InProgressGold, Modifier.weight(1f))
            StatCard("⏳ انتظار", tasks.count { it.status == "قيد الانتظار" }.toString(), PendingAmber, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).testTag("task_search_input"),
            placeholder = { Text("بحث عن مهمة أو عامل...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("الكل", "قيد الانتظار", "جاري العمل", "مكتمل").forEach { status ->
                CustomChip(text = status, selected = statusFilter == status, onClick = { statusFilter = status }, modifier = Modifier.height(36.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("الكل", "عالية", "متوسطة", "منخفضة").forEach { priority ->
                CustomChip(text = priority, selected = priorityFilter == priority, onClick = { priorityFilter = priority },
                    selectedColor = when(priority) { "عالية" -> HighPriorityRed; "متوسطة" -> MediumPriorityOrange; else -> LowPriorityGrey },
                    modifier = Modifier.height(36.dp))
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredTasks, key = { it.id }) { task ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(task.taskName, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.deleteTask(task) }) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red) }
                        }
                        Text("التاريخ: ${DateHelper.formatForDisplay(task.date)}", style = MaterialTheme.typography.bodySmall)
                        Text("المحصول: ${task.cropName}", style = MaterialTheme.typography.bodySmall)
                        Text("العامل: ${task.workerName}", style = MaterialTheme.typography.bodySmall)
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomChip(text = task.priority, selected = true, onClick = {}, selectedColor = when(task.priority) { "عالية" -> HighPriorityRed; "متوسطة" -> MediumPriorityOrange; else -> LowPriorityGrey })
                            CustomChip(text = task.status, selected = true, onClick = {}, selectedColor = when(task.status) { "مكتمل" -> CompletedGreen; "جاري العمل" -> InProgressGold; else -> PendingAmber })
                        }
                        LinearProgressIndicator(progress = { task.progress / 100f }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                        Text("${task.progress}%", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { selectedTask = task; showProgressDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("تعديل التقدم والحالة") }
                    }
                }
            }
        }
    }

    if (showProgressDialog && selectedTask != null) {
        var progress by remember { mutableFloatStateOf(selectedTask!!.progress.toFloat()) }
        var status by remember { mutableStateOf(selectedTask!!.status) }
        AlertDialog(
            onDismissRequest = { showProgressDialog = false },
            title = { Text("تعديل تقدم المهمة") },
            text = {
                Column {
                    Text("المهمة: ${selectedTask!!.taskName}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("نسبة الإنجاز: ${progress.toInt()}%")
                    Slider(value = progress, onValueChange = { progress = it; status = when { it >= 100f -> "مكتمل"; it > 0f -> "جاري العمل"; else -> "قيد الانتظار" } }, valueRange = 0f..100f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("قيد الانتظار", "جاري العمل", "مكتمل").forEach { s ->
                            CustomChip(text = s, selected = status == s, onClick = { status = s; when(s) { "مكتمل" -> progress = 100f; "قيد الانتظار" -> progress = 0f } }, selectedColor = when(s) { "مكتمل" -> CompletedGreen; "جاري العمل" -> InProgressGold; else -> PendingAmber })
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { viewModel.updateTaskProgress(selectedTask!!.id, progress.toInt(), status); showProgressDialog = false }) { Text("حفظ") } },
            dismissButton = { TextButton(onClick = { showProgressDialog = false }) { Text("إلغاء") } }
        )
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var selectedWorker by remember { mutableStateOf(workers.firstOrNull()) }
        var selectedCrop by remember { mutableStateOf(crops.firstOrNull()) }
        var priority by remember { mutableStateOf("متوسطة") }
        var notes by remember { mutableStateOf("") }
        var titleError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة مهمة جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it; titleError = false }, label = { Text("عنوان المهمة") }, isError = titleError, supportingText = { if (titleError) Text("مطلوب", color = Color.Red) }, modifier = Modifier.fillMaxWidth())
                    Text("العامل: ${selectedWorker?.name ?: "غير محدد"}", style = MaterialTheme.typography.bodyMedium)
                    Text("المحصول: ${selectedCrop?.crop ?: "غير محدد"}", style = MaterialTheme.typography.bodyMedium)
                    Text("الأولوية:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("عالية", "متوسطة", "منخفضة").forEach { p ->
                            CustomChip(text = p, selected = priority == p, onClick = { priority = p }, selectedColor = when(p) { "عالية" -> HighPriorityRed; "متوسطة" -> MediumPriorityOrange; else -> LowPriorityGrey })
                        }
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    viewModel.insertTask(FarmTask(date = DateHelper.getCurrentDate(), taskName = title, cropCode = selectedCrop?.code ?: "", cropName = selectedCrop?.crop ?: "", workerCode = selectedWorker?.code ?: "", workerName = selectedWorker?.name ?: "", priority = priority, notes = notes))
                    showAddDialog = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") } }
        )
    }

    FloatingActionButton(
        onClick = { showAddDialog = true },
        modifier = Modifier.padding(16.dp).align(Alignment.End),
        containerColor = MaterialTheme.colorScheme.primary
    ) { Icon(Icons.Default.Add, contentDescription = "إضافة مهمة") }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = color)
        }
    }
}