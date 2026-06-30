package com.ahmedpasha.smartfarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import com.ahmedpasha.smartfarm.util.DateHelper

@Composable
fun HomeDashboard(viewModel: FarmViewModel) {
    val summary by viewModel.summaryData.collectAsState()
    val lowStock by viewModel.lowStockItems.collectAsState()
    val activeDebts by viewModel.activeDebts.collectAsState()
    val todayTasks by viewModel.tasks.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌾 صباح الخير يا باشا", style = MaterialTheme.typography.headlineMedium)
                Text(DateHelper.formatForDisplay(DateHelper.getCurrentDate()), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DashboardCard("المهام", "${summary.totalTasks}", "📋", Color(0xFF3B82F6), Modifier.weight(1f))
            DashboardCard("مكتملة", "${summary.completedTasks}", "✅", Color(0xFF10B981), Modifier.weight(1f))
            DashboardCard("العمال", "${summary.presentWorkers}", "👷", Color(0xFFF59E0B), Modifier.weight(1f))
            DashboardCard("تنبيهات", "${summary.lowStockCount + summary.activeDebtsCount}", "🔔", Color(0xFFEF4444), Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💰 الملخص المالي للشهر", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإيرادات:"); Text("${String.format("%,.0f", summary.monthlyRevenue)} ج", color = Color(0xFF10B981))
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المصروفات:"); Text("${String.format("%,.0f", summary.monthlyExpenses)} ج", color = Color(0xFFEF4444))
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الصافي:", style = MaterialTheme.typography.titleSmall)
                    Text("${String.format("%,.0f", summary.monthlyRevenue - summary.monthlyExpenses)} ج",
                        color = if (summary.monthlyRevenue >= summary.monthlyExpenses) Color(0xFF10B981) else Color(0xFFEF4444))
                }
            }
        }

        if (lowStock.isNotEmpty() || activeDebts.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("⚠️ تنبيهات هامة", style = MaterialTheme.typography.titleSmall, color = Color(0xFFD97706))
                    lowStock.take(3).forEach { item ->
                        Text("• مخزون ${item.name} منخفض (${item.currentBalance} ${item.unit})", style = MaterialTheme.typography.bodySmall)
                    }
                    activeDebts.take(3).forEach { debt ->
                        Text("• دين ${debt.debtType}: ${debt.contactName} - ${debt.amount} ج", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, emoji: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}