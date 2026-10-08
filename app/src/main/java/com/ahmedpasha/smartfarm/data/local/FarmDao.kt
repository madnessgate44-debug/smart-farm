package com.ahmedpasha.smartfarm.data.local

import androidx.room.*
import com.ahmedpasha.smartfarm.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    // Lands
    @Query("SELECT * FROM lands ORDER BY code ASC")
    fun getAllLands(): Flow&lt;List&lt;Land&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLand(land: Land)

    @Delete
    suspend fun deleteLand(land: Land)

    // Crops
    @Query("SELECT * FROM crops ORDER BY code ASC")
    fun getAllCrops(): Flow&lt;List&lt;Crop&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: Crop)

    @Delete
    suspend fun deleteCrop(crop: Crop)

    // Operations
    @Query("SELECT * FROM operations ORDER BY date DESC")
    fun getAllOperations(): Flow&lt;List&lt;Operation&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: Operation)

    @Delete
    suspend fun deleteOperation(operation: Operation)

    // Inventory Items
    @Query("SELECT * FROM inventory_items ORDER BY code ASC")
    fun getAllInventoryItems(): Flow&lt;List&lt;InventoryItem&gt;&gt;

    @Query("SELECT * FROM inventory_items WHERE currentBalance &lt;= minThreshold")
    fun getLowStockItems(): Flow&lt;List&lt;InventoryItem&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    // Inventory Movements
    @Query("SELECT * FROM inventory_movements ORDER BY date DESC")
    fun getAllInventoryMovements(): Flow&lt;List&lt;InventoryMovement&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryMovement(movement: InventoryMovement)

    // Animals
    @Query("SELECT * FROM animals ORDER BY code ASC")
    fun getAllAnimals(): Flow&lt;List&lt;Animal&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal)

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    // Animal Production
    @Query("SELECT * FROM animal_production ORDER BY date DESC")
    fun getAllAnimalProduction(): Flow&lt;List&lt;AnimalProduction&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimalProduction(production: AnimalProduction)

    // Workers
    @Query("SELECT * FROM workers ORDER BY code ASC")
    fun getAllWorkers(): Flow&lt;List&lt;Worker&gt;&gt;

    @Query("SELECT * FROM workers WHERE status = 'نشط'")
    fun getActiveWorkers(): Flow&lt;List&lt;Worker&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow&lt;List&lt;Attendance&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    // Contacts
    @Query("SELECT * FROM contacts ORDER BY code ASC")
    fun getAllContacts(): Flow&lt;List&lt;Contact&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    // Equipment
    @Query("SELECT * FROM equipment ORDER BY code ASC")
    fun getAllEquipment(): Flow&lt;List&lt;Equipment&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: Equipment)

    @Delete
    suspend fun deleteEquipment(equipment: Equipment)

    // Maintenance
    @Query("SELECT * FROM maintenance ORDER BY date DESC")
    fun getAllMaintenance(): Flow&lt;List&lt;Maintenance&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: Maintenance)

    // Water Logs
    @Query("SELECT * FROM water_logs ORDER BY date DESC")
    fun getAllWaterLogs(): Flow&lt;List&lt;WaterLog&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLog)

    // Purchases
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchases(): Flow&lt;List&lt;Purchase&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Delete
    suspend fun deletePurchase(purchase: Purchase)

    // Sales
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow&lt;List&lt;Sale&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Delete
    suspend fun deleteSale(sale: Sale)

    // Treasury
    @Query("SELECT * FROM treasury_transactions ORDER BY date DESC")
    fun getAllTreasuryTransactions(): Flow&lt;List&lt;TreasuryTransaction&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreasuryTransaction(transaction: TreasuryTransaction)

    @Query("DELETE FROM treasury_transactions WHERE sourceType = :sourceType AND sourceId = :sourceId")
    suspend fun deleteTreasuryTransactionsBySource(sourceType: String, sourceId: Int)

    // Debts
    @Query("SELECT * FROM debts ORDER BY date DESC")
    fun getAllDebts(): Flow&lt;List&lt;Debt&gt;&gt;

    @Query("SELECT * FROM debts WHERE status != 'مسدد بالكامل'")
    fun getActiveDebts(): Flow&lt;List&lt;Debt&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)

    // Meetings
    @Query("SELECT * FROM meetings ORDER BY date DESC")
    fun getAllMeetings(): Flow&lt;List&lt;Meeting&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting)

    @Delete
    suspend fun deleteMeeting(meeting: Meeting)

    // Preferences
    @Query("SELECT * FROM ahmed_preferences ORDER BY id ASC")
    fun getAllPreferences(): Flow&lt;List&lt;AhmedPreference&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: AhmedPreference)

    // Tasks
    @Query("SELECT * FROM farm_tasks ORDER BY date DESC")
    fun getAllTasks(): Flow&lt;List&lt;FarmTask&gt;&gt;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FarmTask)

    @Query("UPDATE farm_tasks SET progress = :progress, status = :status WHERE id = :taskId")
    suspend fun updateTaskProgress(taskId: Int, progress: Int, status: String)

    @Delete
    suspend fun deleteTask(task: FarmTask)

    // Analytics
    @Query("SELECT COUNT(*) FROM farm_tasks WHERE status = 'مكتمل'")
    fun getCompletedTasksCount(): Flow&lt;Int&gt;

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'حاضر'")
    fun getPresentWorkersCount(date: String): Flow&lt;Int&gt;

    @Query("SELECT SUM(totalRevenue) FROM sales WHERE date LIKE :month")
    fun getMonthlySales(month: String): Flow&lt;Double?&gt;

    @Query("SELECT SUM(totalCost) FROM purchases WHERE date LIKE :month")
    fun getMonthlyPurchases(month: String): Flow&lt;Double?&gt;

    // ============ Transactional Business Operations ============

    @Transaction
    suspend fun recordPurchase(purchase: Purchase): Long {
        val purchaseId = dao.insertPurchase(purchase)
        if (purchase.paid > 0) {
            val transaction = TreasuryTransaction(
                date = purchase.date,
                transactionType = "صرف",
                amount = purchase.paid,
                category = "مشتريات",
                description = "شراء ${purchase.item}",
                sourceType = "PURCHASE",
                sourceId = purchaseId.toInt()
            )
            dao.insertTreasuryTransaction(transaction)
        }
        return purchaseId
    }

    @Transaction
    suspend fun recordSale(sale: Sale): Long {
        val saleId = dao.insertSale(sale)
        if (sale.received > 0) {
            val transaction = TreasuryTransaction(
                date = sale.date,
                transactionType = "إيداع",
                amount = sale.received,
                category = "مبيعات",
                description = "بيع ${sale.item}",
                sourceType = "SALE",
                sourceId = saleId.toInt()
            )
            dao.insertTreasuryTransaction(transaction)
        }
        return saleId
    }

    @Transaction
    suspend fun deletePurchaseAndReverse(purchase: Purchase) {
        dao.deleteTreasuryTransactionsBySource("PURCHASE", purchase.id)
        dao.deletePurchase(purchase)
    }

    @Transaction
    suspend fun deleteSaleAndReverse(sale: Sale) {
        dao.deleteTreasuryTransactionsBySource("SALE", sale.id)
        dao.deleteSale(sale)
    }

    @Query("SELECT * FROM inventory_items WHERE code = :itemCode")
    suspend fun getInventoryItemForUpdate(itemCode: String): InventoryItem?

    @Query("UPDATE inventory_items SET currentBalance = currentBalance + :delta WHERE code = :itemCode AND (currentBalance + :delta) >= 0")
    suspend fun guardedUpdateBalance(itemCode: String, delta: Double): Int

    @Transaction
    suspend fun recordInventoryMovement(movement: InventoryMovement) {
        val isIncoming = listOf("إضافة", "استلام", "دخول", "in", "add", "purchase")
            .any { movement.movementType.contains(it, ignoreCase = true) }
        val delta = if (isIncoming) movement.quantity else -movement.quantity

        val updated = dao.guardedUpdateBalance(movement.itemCode, delta)
        if (updated == 0) {
            throw IllegalStateException("Insufficient balance for item ${movement.itemCode}. Cannot decrease below zero.")
        }
        dao.insertInventoryMovement(movement)
    }
}
