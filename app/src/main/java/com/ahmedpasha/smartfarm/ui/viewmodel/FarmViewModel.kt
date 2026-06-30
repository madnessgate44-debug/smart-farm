package com.ahmedpasha.smartfarm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedpasha.smartfarm.FarmApplication
import com.ahmedpasha.smartfarm.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FarmApplication).repository

    val lands = repository.allLands.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val crops = repository.allCrops.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val operations = repository.allOperations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inventoryItems = repository.allInventoryItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inventoryMovements = repository.allInventoryMovements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val animals = repository.allAnimals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val animalProduction = repository.allAnimalProduction.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workers = repository.allWorkers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val attendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val contacts = repository.allContacts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val equipment = repository.allEquipment.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val maintenance = repository.allMaintenance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val waterLogs = repository.allWaterLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val treasury = repository.allTreasuryTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val debts = repository.allDebts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val meetings = repository.allMeetings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockItems = repository.lowStockItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeDebts = repository.activeDebts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeWorkers = repository.activeWorkers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow("أراضي")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    val summaryData = combine(
        tasks, attendance, inventoryItems, debts, purchases, sales
    ) { tasks, attendance, inventory, debts, purchases, sales ->
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val month = today.substring(0, 7)
        DashboardSummary(
            totalTasks = tasks.size,
            completedTasks = tasks.count { it.status == "مكتمل" },
            inProgressTasks = tasks.count { it.status == "جاري العمل" },
            pendingTasks = tasks.count { it.status == "قيد الانتظار" },
            presentWorkers = attendance.count { it.date == today && it.status == "حاضر" },
            lowStockCount = inventory.count { it.currentBalance <= it.minThreshold },
            activeDebtsCount = debts.count { it.status != "مسدد بالكامل" },
            monthlyRevenue = sales.filter { it.date.startsWith(month) }.sumOf { it.totalRevenue },
            monthlyExpenses = purchases.filter { it.date.startsWith(month) }.sumOf { it.totalCost }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun setSelectedTab(tab: String) { _selectedTab.value = tab }

    fun addChatMessage(message: String, isUser: Boolean) {
        _chatMessages.update { it + (message to isUser) }
        if (isUser) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                val response = when {
                    message.contains("شراء") -> "تمام يا باشا، فهمت إنك اشتريت حاجة. ممكن تفاصيل الصنف والكمية والسعر؟ 📝"
                    message.contains("بيع") -> "ماشي يا باشا، عملية بيع. قولي اتباع إيه وبكام؟ 💰"
                    message.contains("حضر") || message.contains("عمال") -> "حاضر يا باشا، هسجل حضور العمال النهارده 👷"
                    message.contains("ري") -> "أوكيه، عملية ري. أي قطعة وكم ساعة؟ 💧"
                    else -> "الأستاذ أحمد يا باشا، أمرك حاضر. قولي إيه اللي حصل في المزرعة وأنا أسجله ✓"
                }
                _chatMessages.update { it + (response to false) }
            }
        }
    }

    fun insertLand(land: Land) = viewModelScope.launch { repository.insertLand(land) }
    fun deleteLand(land: Land) = viewModelScope.launch { repository.deleteLand(land) }

    fun insertCrop(crop: Crop) = viewModelScope.launch { repository.insertCrop(crop) }
    fun deleteCrop(crop: Crop) = viewModelScope.launch { repository.deleteCrop(crop) }

    fun insertOperation(operation: Operation) = viewModelScope.launch { repository.insertOperation(operation) }

    fun insertInventoryItem(item: InventoryItem) = viewModelScope.launch { repository.insertInventoryItem(item) }
    fun deleteInventoryItem(item: InventoryItem) = viewModelScope.launch { repository.deleteInventoryItem(item) }

    fun insertInventoryMovement(movement: InventoryMovement) = viewModelScope.launch { repository.insertInventoryMovement(movement) }

    fun insertAnimal(animal: Animal) = viewModelScope.launch { repository.insertAnimal(animal) }
    fun deleteAnimal(animal: Animal) = viewModelScope.launch { repository.deleteAnimal(animal) }

    fun insertAnimalProduction(production: AnimalProduction) = viewModelScope.launch { repository.insertAnimalProduction(production) }

    fun insertWorker(worker: Worker) = viewModelScope.launch { repository.insertWorker(worker) }
    fun deleteWorker(worker: Worker) = viewModelScope.launch { repository.deleteWorker(worker) }

    fun insertAttendance(attendance: Attendance) = viewModelScope.launch { repository.insertAttendance(attendance) }

    fun insertContact(contact: Contact) = viewModelScope.launch { repository.insertContact(contact) }
    fun deleteContact(contact: Contact) = viewModelScope.launch { repository.deleteContact(contact) }

    fun insertEquipment(equipment: Equipment) = viewModelScope.launch { repository.insertEquipment(equipment) }
    fun deleteEquipment(equipment: Equipment) = viewModelScope.launch { repository.deleteEquipment(equipment) }

    fun insertMaintenance(maintenance: Maintenance) = viewModelScope.launch { repository.insertMaintenance(maintenance) }

    fun insertWaterLog(waterLog: WaterLog) = viewModelScope.launch { repository.insertWaterLog(waterLog) }

    fun insertPurchase(purchase: Purchase) = viewModelScope.launch {
        repository.insertPurchase(purchase)
        repository.insertTreasuryTransaction(TreasuryTransaction(
            date = purchase.date, transactionType = "صرف", amount = purchase.totalCost,
            category = "مشتريات", description = "شراء ${purchase.item}"
        ))
    }
    fun deletePurchase(purchase: Purchase) = viewModelScope.launch { repository.deletePurchase(purchase) }

    fun insertSale(sale: Sale) = viewModelScope.launch {
        repository.insertSale(sale)
        repository.insertTreasuryTransaction(TreasuryTransaction(
            date = sale.date, transactionType = "إيداع", amount = sale.totalRevenue,
            category = "مبيعات", description = "بيع ${sale.item}"
        ))
    }
    fun deleteSale(sale: Sale) = viewModelScope.launch { repository.deleteSale(sale) }

    fun insertTreasuryTransaction(transaction: TreasuryTransaction) = viewModelScope.launch { repository.insertTreasuryTransaction(transaction) }

    fun insertDebt(debt: Debt) = viewModelScope.launch { repository.insertDebt(debt) }

    fun insertMeeting(meeting: Meeting) = viewModelScope.launch { repository.insertMeeting(meeting) }
    fun deleteMeeting(meeting: Meeting) = viewModelScope.launch { repository.deleteMeeting(meeting) }

    fun insertTask(task: FarmTask) = viewModelScope.launch { repository.insertTask(task) }
    fun updateTaskProgress(taskId: Int, progress: Int, status: String) = viewModelScope.launch { repository.updateTaskProgress(taskId, progress, status) }
    fun deleteTask(task: FarmTask) = viewModelScope.launch { repository.deleteTask(task) }

    fun markAllWorkersPresent(date: String) = viewModelScope.launch {
        workers.value.forEach { worker ->
            repository.insertAttendance(Attendance(workerCode = worker.code, date = date, status = "حاضر"))
        }
    }
}

data class DashboardSummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val pendingTasks: Int = 0,
    val presentWorkers: Int = 0,
    val lowStockCount: Int = 0,
    val activeDebtsCount: Int = 0,
    val monthlyRevenue: Double = 0.0,
    val monthlyExpenses: Double = 0.0
)