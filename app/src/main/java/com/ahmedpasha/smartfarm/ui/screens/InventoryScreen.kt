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

@Composable
fun InventoryScreen(viewModel: FarmViewModel) {
    val items by viewModel.inventoryItems.collectAsState()
    val movements by viewModel.inventoryMovements.collectAsState()
    val lowStock by viewModel.lowStockItems.collectAsState()
    var selectedTab by remember { mutableStateOf("المخزون") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📦 إدارة المخازن", style = MaterialTheme.typography.headlineMedium)

        if (lowStock.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("⚠️ تنبيه نفاد مخزون", style = MaterialTheme.typography.titleSmall, color = Color(0xFFDC2626))
                        lowStock.forEach { item -> Text("• ${item.name}: متبقي ${item.currentBalance} ${item.unit}", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomChip("المخزون", selected = selectedTab == "المخزون", onClick = { selectedTab = "المخزون" })
            CustomChip("الحركات", selected = selectedTab == "الحركات", onClick = { selectedTab = "الحركات" })
        }

        if (selectedTab == "المخزون") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall)
                                Text("${item.category} • ${item.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                LinearProgressIndicator(progress = { (item.currentBalance / item.initialBalance).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(0.5f).padding(vertical = 2.dp), color = when { item.currentBalance <= item.minThreshold -> Color(0xFFDC2626); item.currentBalance <= item.minThreshold * 2 -> Color(0xFFF59E0B); else -> Color(0xFF10B981) })
                                Text("${item.currentBalance} / ${item.initialBalance} ${item.unit}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.deleteInventoryItem(item) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(movements.take(50)) { mov ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text(mov.itemCode, style = MaterialTheme.typography.bodySmall); Text(mov.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                            Text("${if (mov.movementType == "وارد") "+" else "-"}${mov.quantity}", color = if (mov.movementType == "وارد") Color(0xFF10B981) else Color(0xFFEF4444), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}