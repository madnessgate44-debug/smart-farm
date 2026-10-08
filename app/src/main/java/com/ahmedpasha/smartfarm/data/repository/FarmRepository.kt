package com.ahmedpasha.smartfarm.data.repository

import com.ahmedpasha.smartfarm.data.local.FarmDao
import com.ahmedpasha.smartfarm.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FarmRepository(private val dao: FarmDao) {

    val allLands: Flow&lt;List&lt;Land&gt;&gt; = dao.getAllLands()
    suspend fun insertLand(land: Land) = withContext(Dispatchers.IO) { dao.insertLand(land) }
    suspend fun deleteLand(land: Land) = withContext(Dispatchers.IO) { dao.deleteLand(land) }

    val allCrops: Flow&lt;List&lt;Crop&gt;&gt; = dao.getAllCrops()
    suspend fun insertCrop(crop: Crop) = withContext(Dispatchers.IO) { dao.insertCrop(crop) }
    suspend fun deleteCrop(crop: Crop) = withContext(Dispatchers.IO) { dao.deleteCrop(crop) }

    val allOperations: Flow&lt;List&lt;Operation&gt;&gt; = dao.getAllOperations()
    suspend fun insertOperation(operation: Operation) = withContext(Dispatchers.IO) { dao.insertOperation(operation) }
    suspend fun deleteOperation(operation: Operation) = withContext(Dispatchers.IO) { dao.deleteOperation(operation) }

    val allInventoryItems: Flow&lt;List&lt;InventoryItem&gt;&gt; = dao.getAllInventoryItems()
    val lowStockItems: Flow&lt;List&lt;InventoryItem&gt;&gt; = dao.getLowStockItems()
    suspend fun insertInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) { dao.insertInventoryItem(item) }
    suspend fun deleteInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) { dao.deleteInventoryItem(item) }

    val allInventoryMovements: Flow&lt;List&lt;InventoryMovement&gt;&gt; = dao.getAllInventoryMovements()
    suspend fun recordInventoryMovement(movement: InventoryMovement) = withContext(Dispatchers.IO) { dao.recordInventoryMovement(movement) }

    val allAnimals: Flow&lt;List&lt;Animal&gt;&gt; = dao.getAllAnimals()
    suspend fun insertAnimal(animal: Animal) = withContext(Dispatchers.IO) { dao.insertAnimal(animal) }
    suspend fun deleteAnimal(animal: Animal) = withContext(Dispatchers.IO) { dao.deleteAnimal(animal) }

    val allAnimalProduction: Flow&lt;List&lt;AnimalProduction&gt;&gt; = dao.getAllAnimalProduction()
    suspend fun insertAnimalProduction(production: AnimalProduction) = withContext(Dispatchers.IO) { dao.insertAnimalProduction(production) }

    val allWorkers: Flow&lt;List&lt;Worker&gt;&gt; = dao.getAllWorkers()
    val activeWorkers: Flow&lt;List&lt;Worker&gt;&gt; = dao.getActiveWorkers()
    suspend fun insertWorker(worker: Worker) = withContext(Dispatchers.IO) { dao.insertWorker(worker) }
    suspend fun deleteWorker(worker: Worker) = withContext(Dispatchers.IO) { dao.deleteWorker(worker) }

    val allAttendance: Flow&lt;List&lt;Attendance&gt;&gt; = dao.getAllAttendance()
    suspend fun insertAttendance(attendance: Attendance) = withContext(Dispatchers.IO) { dao.insertAttendance(attendance) }

    val allContacts: Flow&lt;List&lt;Contact&gt;&gt; = dao.getAllContacts()
    suspend fun insertContact(contact: Contact) = withContext(Dispatchers.IO) { dao.insertContact(contact) }
    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) { dao.deleteContact(contact) }

    val allEquipment: Flow&lt;List&lt;Equipment&gt;&gt; = dao.getAllEquipment()
    suspend fun insertEquipment(equipment: Equipment) = withContext(Dispatchers.IO) { dao.insertEquipment(equipment) }
    suspend fun deleteEquipment(equipment: Equipment) = withContext(Dispatchers.IO) { dao.deleteEquipment(equipment) }

    val allMaintenance: Flow&lt;List&lt;Maintenance&gt;&gt; = dao.getAllMaintenance()
    suspend fun insertMaintenance(maintenance: Maintenance) = withContext(Dispatchers.IO) { dao.insertMaintenance(maintenance) }

    val allWaterLogs: Flow&lt;List&lt;WaterLog&gt;&gt; = dao.getAllWaterLogs()
    suspend fun insertWaterLog(waterLog: WaterLog) = withContext(Dispatchers.IO) { dao.insertWaterLog(waterLog) }

    val allPurchases: Flow&lt;List&lt;Purchase&gt;&gt; = dao.getAllPurchases()
    suspend fun recordPurchase(purchase: Purchase) = withContext(Dispatchers.IO) { dao.recordPurchase(purchase) }
    suspend fun deletePurchase(purchase: Purchase) = withContext(Dispatchers.IO) { dao.deletePurchaseAndReverse(purchase) }

    val allSales: Flow&lt;List&lt;Sale&gt;&gt; = dao.getAllSales()
    suspend fun recordSale(sale: Sale) = withContext(Dispatchers.IO) { dao.recordSale(sale) }
    suspend fun deleteSale(sale: Sale) = withContext(Dispatchers.IO) { dao.deleteSaleAndReverse(sale) }

    val allTreasuryTransactions: Flow&lt;List&lt;TreasuryTransaction&gt;&gt; = dao.getAllTreasuryTransactions()
    suspend fun insertTreasuryTransaction(transaction: TreasuryTransaction) = withContext(Dispatchers.IO) { dao.insertTreasuryTransaction(transaction) }

    val allDebts: Flow&lt;List&lt;Debt&gt;&gt; = dao.getAllDebts()
    val activeDebts: Flow&lt;List&lt;Debt&gt;&gt; = dao.getActiveDebts()
    suspend fun insertDebt(debt: Debt) = withContext(Dispatchers.IO) { dao.insertDebt(debt) }

    val allMeetings: Flow&lt;List&lt;Meeting&gt;&gt; = dao.getAllMeetings()
    suspend fun insertMeeting(meeting: Meeting) = withContext(Dispatchers.IO) { dao.insertMeeting(meeting) }
    suspend fun deleteMeeting(meeting: Meeting) = withContext(Dispatchers.IO) { dao.deleteMeeting(meeting) }

    val allPreferences: Flow&lt;List&lt;AhmedPreference&gt;&gt; = dao.getAllPreferences()
    suspend fun insertPreference(preference: AhmedPreference) = withContext(Dispatchers.IO) { dao.insertPreference(preference) }

    val allTasks: Flow&lt;List&lt;FarmTask&gt;&gt; = dao.getAllTasks()
    val completedTasksCount: Flow&lt;Int&gt; = dao.getCompletedTasksCount()
    suspend fun insertTask(task: FarmTask) = withContext(Dispatchers.IO) { dao.insertTask(task) }
    suspend fun updateTaskProgress(taskId: Int, progress: Int, status: String) = withContext(Dispatchers.IO) { dao.updateTaskProgress(taskId, progress, status) }
    suspend fun deleteTask(task: FarmTask) = withContext(Dispatchers.IO) { dao.deleteTask(task) }

    fun getPresentWorkersCount(date: String): Flow&lt;Int&gt; = dao.getPresentWorkersCount(date)
    fun getMonthlySales(month: String): Flow&lt;Double?&gt; = dao.getMonthlySales(month)
    fun getMonthlyPurchases(month: String): Flow&lt;Double?&gt; = dao.getMonthlyPurchases(month)
}
