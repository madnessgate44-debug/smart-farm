package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.models.*
import com.ahmedpasha.smartfarm.data.repository.FarmRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BaleghFarmState(
    val lands: List<Land> = emptyList(),
    val crops: List<Crop> = emptyList(),
    val operations: List<Operation> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val lowStock: List<InventoryItem> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val maintenance: List<Maintenance> = emptyList(),
    val waterLogs: List<WaterLog> = emptyList(),
    val animals: List<Animal> = emptyList(),
    val animalProduction: List<AnimalProduction> = emptyList(),
    val tasks: List<FarmTask> = emptyList(),
    val purchases: List<Purchase> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val treasury: List<TreasuryTransaction> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val contacts: List<Contact> = emptyList()
) {
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    fun inventoryByName(query: String): InventoryItem? = inventory.firstOrNull { it.name.equals(query, true) || it.code.equals(query, true) }
    fun workerByName(query: String): Worker? = workers.firstOrNull { it.name.equals(query, true) || it.code.equals(query, true) }
    fun landByName(query: String): Land? = lands.firstOrNull { it.name.equals(query, true) || it.code.equals(query, true) }
    fun cropByCode(query: String): Crop? = crops.firstOrNull { it.code.equals(query, true) || it.crop.equals(query, true) }
}

class BaleghFarmStateProvider(private val repository: FarmRepository) {
    suspend fun loadFor(intent: BaleghIntent, text: String = ""): BaleghFarmState {
        val t = text.lowercase(Locale.ROOT)
        val inventoryNeeded = intent == BaleghIntent.INVENTORY_QUERY || intent == BaleghIntent.PURCHASE_ACTION || t.contains("مخزن") || t.contains("سماد") || t.contains("مستلزمات")
        val workersNeeded = intent == BaleghIntent.WORKER_QUERY || intent == BaleghIntent.CREATE_TASK || intent == BaleghIntent.COMMITMENT || t.contains("عامل") || t.contains("عمال")
        val cropsNeeded = intent == BaleghIntent.CROP_QUERY || t.contains("محصول") || t.contains("زراعة") || t.contains("قمح") || t.contains("ذرة")
        val landNeeded = intent == BaleghIntent.LAND_QUERY || t.contains("أرض") || t.contains("قطعة")
        val equipmentNeeded = intent == BaleghIntent.PROBLEM_REPORT || t.contains("طلمبة") || t.contains("معدات") || t.contains("صيانة")
        val financeNeeded = intent == BaleghIntent.FINANCE_QUERY || intent == BaleghIntent.PURCHASE_ACTION || intent == BaleghIntent.SALE_ACTION || t.contains("فلوس") || t.contains("تكلفة") || t.contains("مصروف")
        val tasksNeeded = intent == BaleghIntent.CREATE_TASK || intent == BaleghIntent.COMMITMENT || t.contains("مهمة") || t.contains("بكرة")

        return BaleghFarmState(
            lands = if (landNeeded || cropsNeeded) repository.allLands.first() else emptyList(),
            crops = if (cropsNeeded || landNeeded) repository.allCrops.first() else emptyList(),
            operations = if (cropsNeeded || landNeeded || intent == BaleghIntent.PROBLEM_REPORT) repository.allOperations.first() else emptyList(),
            inventory = if (inventoryNeeded) repository.allInventoryItems.first() else emptyList(),
            lowStock = if (inventoryNeeded) repository.lowStockItems.first() else emptyList(),
            workers = if (workersNeeded) repository.allWorkers.first() else emptyList(),
            attendance = if (workersNeeded) repository.allAttendance.first() else emptyList(),
            equipment = if (equipmentNeeded) repository.allEquipment.first() else emptyList(),
            maintenance = if (equipmentNeeded) repository.allMaintenance.first() else emptyList(),
            waterLogs = if (t.contains("ري") || t.contains("مياه") || intent == BaleghIntent.PROBLEM_REPORT) repository.allWaterLogs.first() else emptyList(),
            animals = if (t.contains("حيوان") || t.contains("مواشي") || t.contains("ماشية")) repository.allAnimals.first() else emptyList(),
            animalProduction = if (t.contains("إنتاج") || t.contains("لبن") || t.contains("بيض")) repository.allAnimalProduction.first() else emptyList(),
            tasks = if (tasksNeeded || intent == BaleghIntent.FARM_STATUS) repository.allTasks.first() else emptyList(),
            purchases = if (financeNeeded) repository.allPurchases.first() else emptyList(),
            sales = if (financeNeeded) repository.allSales.first() else emptyList(),
            treasury = if (financeNeeded) repository.allTreasuryTransactions.first() else emptyList(),
            debts = if (financeNeeded) repository.allDebts.first() else emptyList(),
            contacts = if (financeNeeded || intent == BaleghIntent.GENERAL_QUERY) repository.allContacts.first() else emptyList()
        )
    }
}
