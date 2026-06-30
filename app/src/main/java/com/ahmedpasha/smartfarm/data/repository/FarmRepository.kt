package com.ahmedpasha.smartfarm.data.repository

import com.ahmedpasha.smartfarm.data.local.FarmDao
import com.ahmedpasha.smartfarm.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FarmRepository(private val dao: FarmDao) {
    
    val allLands: Flow<List<Land>> = dao.getAllLands()
    suspend fun insertLand(land: Land) = withContext(Dispatchers.IO) { dao.insertLand(land) }
    suspend fun deleteLand(land: Land) = withContext(Dispatchers.IO) { dao.deleteLand(land) }

    val allCrops: Flow<List<Crop>> = dao.getAllCrops()
    suspend fun insertCrop(crop: Crop) = withContext(Dispatchers.IO) { dao.insertCrop(crop) }
    suspend fun deleteCrop(crop: Crop) = withContext(Dispatchers.IO) { dao.deleteCrop(crop) }

    val allOperations: Flow<List<Operation>> = dao.getAllOperations()
    suspend fun insertOperation(operation: Operation) = withContext(Dispatchers.IO) { dao.insertOperation(operation) }
    suspend fun deleteOperation(operation: Operation) = withContext(Dispatchers.IO) { dao.deleteOperation(operation) }

    val allInventoryItems: Flow<List<InventoryItem>> = dao.getAllInventoryItems()
    val lowStockItems: Flow<List<InventoryItem>> = dao.getLowStockItems()
    suspend fun insertInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) { dao.insertInventoryItem(item) }
    suspend fun deleteInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) { dao.deleteInventoryItem(item) }

    val allInventoryMovements: Flow<List<InventoryMovement>> = dao.getAllInventoryMovements()
    suspend fun insertInventoryMovement(movement: InventoryMovement) = withContext(Dispatchers.IO) { dao.insertInventoryMovement(movement) }

    val allAnimals: Flow<List<Animal>> = dao.getAllAnimals()
    suspend fun insertAnimal(animal: Animal) = withContext(Dispatchers.IO) { dao.insertAnimal(animal) }
    suspend fun deleteAnimal(animal: Animal) = withContext(Dispatchers.IO) { dao.deleteAnimal(animal) }

    val allAnimalProduction: Flow<List<AnimalProduction>> = dao.getAllAnimalProduction()
    suspend fun insertAnimalProduction(production: AnimalProduction) = withContext(Dispatchers.IO) { dao.insertAnimalProduction(production) }

    val allWorkers: Flow<List<Worker>> = dao.getAllWorkers()
    val activeWorkers: Flow<List<Worker>> = dao.getActiveWorkers()
    suspend fun insertWorker(worker: Worker) = withContext(Dispatchers.IO) { dao.insertWorker(worker) }
    suspend fun deleteWorker(worker: Worker) = withContext(Dispatchers.IO) { dao.deleteWorker(worker) }

    val allAttendance: Flow<List<Attendance>> = dao.getAllAttendance()
    suspend fun insertAttendance(attendance: Attendance) = withContext(Dispatchers.IO) { dao.insertAttendance(attendance) }

    val allContacts: Flow<List<Contact>> = dao.getAllContacts()
    suspend fun insertContact(contact: Contact) = withContext(Dispatchers.IO) { dao.insertContact(contact) }
    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) { dao.deleteContact(contact) }

    val allEquipment: Flow<List<Equipment>> = dao.getAllEquipment()
    suspend fun insertEquipment(equipment: Equipment) = withContext(Dispatchers.IO) { dao.insertEquipment(equipment) }
    suspend fun deleteEquipment(equipment: Equipment) = withContext(Dispatchers.IO) { dao.deleteEquipment(equipment) }

    val allMaintenance: Flow<List<Maintenance>> = dao.getAllMaintenance()
    suspend fun insertMaintenance(maintenance: Maintenance) = withContext(Dispatchers.IO) { dao.insertMaintenance(maintenance) }

    val allWaterLogs: Flow<List<WaterLog>> = dao.getAllWaterLogs()
    suspend fun insertWaterLog(waterLog: WaterLog) = withContext(Dispatchers.IO) { dao.insertWaterLog(waterLog) }

    val allPurchases: Flow<List<Purchase>> = dao.getAllPurchases()
    suspend fun insertPurchase(purchase: Purchase) = withContext(Dispatchers.IO) { dao.insertPurchase(purchase) }
    suspend fun deletePurchase(purchase: Purchase) = withContext(Dispatchers.IO) { dao.deletePurchase(purchase) }

    val allSales: Flow<List<Sale>> = dao.getAllSales()
    suspend fun insertSale(sale: Sale) = withContext(Dispatchers.IO) { dao.insertSale(sale) }
    suspend fun deleteSale(sale: Sale) = withContext(Dispatchers.IO) { dao.deleteSale(sale) }

    val allTreasuryTransactions: Flow<List<TreasuryTransaction>> = dao.getAllTreasuryTransactions()
    suspend fun insertTreasuryTransaction(transaction: TreasuryTransaction) = withContext(Dispatchers.IO) { dao.insertTreasuryTransaction(transaction) }

    val allDebts: Flow<List<Debt>> = dao.getAllDebts()
    val activeDebts: Flow<List<Debt>> = dao.getActiveDebts()
    suspend fun insertDebt(debt: Debt) = withContext(Dispatchers.IO) { dao.insertDebt(debt) }

    val allMeetings: Flow<List<Meeting>> = dao.getAllMeetings()
    suspend fun insertMeeting(meeting: Meeting) = withContext(Dispatchers.IO) { dao.insertMeeting(meeting) }
    suspend fun deleteMeeting(meeting: Meeting) = withContext(Dispatchers.IO) { dao.deleteMeeting(meeting) }

    val allPreferences: Flow<List<AhmedPreference>> = dao.getAllPreferences()
    suspend fun insertPreference(preference: AhmedPreference) = withContext(Dispatchers.IO) { dao.insertPreference(preference) }

    val allTasks: Flow<List<FarmTask>> = dao.getAllTasks()
    val completedTasksCount: Flow<Int> = dao.getCompletedTasksCount()
    suspend fun insertTask(task: FarmTask) = withContext(Dispatchers.IO) { dao.insertTask(task) }
    suspend fun updateTaskProgress(taskId: Int, progress: Int, status: String) = withContext(Dispatchers.IO) { dao.updateTaskProgress(taskId, progress, status) }
    suspend fun deleteTask(task: FarmTask) = withContext(Dispatchers.IO) { dao.deleteTask(task) }

    fun getPresentWorkersCount(date: String): Flow<Int> = dao.getPresentWorkersCount(date)
    fun getMonthlySales(month: String): Flow<Double?> = dao.getMonthlySales(month)
    fun getMonthlyPurchases(month: String): Flow<Double?> = dao.getMonthlyPurchases(month)
}