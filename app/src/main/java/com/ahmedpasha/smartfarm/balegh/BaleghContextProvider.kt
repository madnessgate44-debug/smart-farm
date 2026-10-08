package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.repository.FarmRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BaleghContextProvider(private val repository: FarmRepository) {
    suspend fun getContextFor(text: String): TargetedFarmContext {
        val lower = text.lowercase(Locale.ROOT)
        val needWorkers = listOf("عامل","عمال","مهمة","كلف","هيجيب","وعد","سيلتزم","حضر").any(lower::contains)
        val needInventory = listOf("مخزن","ناقص","سماد","مستلزمات","شراء","اشتريت").any(lower::contains)
        val needCrops = listOf("محصول","قمح","ذرة","أرز","بيع","بعنا").any(lower::contains)
        val needFinance = listOf("مصروف","مصاريف","المبيعات","الخزينة","تكلفة","فلوس").any(lower::contains)
        val workers = if (needWorkers) repository.allWorkers.first() else emptyList()
        val inventory = if (needInventory) repository.allInventoryItems.first() else emptyList()
        val lowStock = if (needInventory) repository.lowStockItems.first() else emptyList()
        val crops = if (needCrops) repository.allCrops.first() else emptyList()
        val lands = if (text.contains("أرض") || text.contains("قطعة")) repository.allLands.first() else emptyList()
        val tasks = if (text.contains("مهمة")) repository.allTasks.first() else emptyList()
        val month = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val purchases = if (needFinance) repository.getMonthlyPurchases("$month%").first() ?: 0.0 else 0.0
        val sales = if (needFinance) repository.getMonthlySales("$month%").first() ?: 0.0 else 0.0
        return TargetedFarmContext(workers, inventory, lowStock, crops, lands, tasks, purchases, sales)
    }
}
