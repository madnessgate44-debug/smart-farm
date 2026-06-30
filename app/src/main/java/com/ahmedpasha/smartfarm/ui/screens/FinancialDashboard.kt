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

@Composable
fun FinancialDashboard(viewModel: FarmViewModel) {
    val summary by viewModel.summaryData.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val debts by viewModel.activeDebts.collectAsState()
    val treasury by viewModel.treasury.collectAsState()

    val netProfit = summary.monthlyRevenue - summary.monthlyExpenses
    val totalTreasury = treasury.sumOf { if (it.transactionType == "إيداع") it.amount else -it.amount }
    val totalDebtsOnUs = debts.filter { it.debtType == "علينا" }.sumOf { it.amount }
    val totalDebtsToUs = debts.filter { it.debtType == "لنا" }.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("💰 لوحة التحكم المالية", style = MaterialTheme.typography.headlineMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FinancialCard("الإيرادات", "${String.format("%,.0f", summary.monthlyRevenue)} ج", Color(0xFF10B981), Modifier.weight(1f))
            FinancialCard("المصروفات", "${String.format("%,.0f", summary.monthlyExpenses)} ج", Color(0xFFEF4444), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FinancialCard("صافي الربح", "${String.format("%,.0f", netProfit)} ج", if (netProfit >= 0) Color(0xFF059669) else Color.Red, Modifier.weight(1f))
            FinancialCard("رصيد الخزينة", "${String.format("%,.0f", totalTreasury)} ج", Color(0xFF3B82F6), Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💳 ملخص الديون", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween) { Text("ديون لنا:"); Text("${String.format("%,.0f", totalDebtsToUs)} ج", color = Color(0xFF10B981)) }
                Row(horizontalArrangement = Arrangement.SpaceBetween) { Text("ديون علينا:"); Text("${String.format("%,.0f", totalDebtsOnUs)} ج", color = Color(0xFFEF4444)) }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🔄 آخر المعاملات", style = MaterialTheme.typography.titleMedium)
                val allTransactions = (sales.map { it.date to "بيع: ${it.item} (+${it.totalRevenue} ج)" } + purchases.map { it.date to "شراء: ${it.item} (-${it.totalCost} ج)" }).sortedByDescending { it.first }.take(10)
                allTransactions.forEach { (date, desc) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(desc, style = MaterialTheme.typography.bodySmall); Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun FinancialCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}