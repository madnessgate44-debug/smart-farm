package com.ahmedpasha.smartfarm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedpasha.smartfarm.FarmApplication
import com.ahmedpasha.smartfarm.balegh.*
import com.ahmedpasha.smartfarm.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FarmApplication).repository
    private val baleghEngine = BaleghEngine(repository)

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

    private val _baleghUiState = MutableStateFlow<BaleghUiState>(BaleghUiState.Idle)
    val baleghUiState: StateFlow<BaleghUiState> = _baleghUiState.asStateFlow()

    private val _baleghMessages = MutableStateFlow(
        listOf(BaleghChatMessage(
            sender = BaleghChatMessage.Sender.BALEGH,
            text = "أهلاً بك يا أستاذ أحمد. أنا بليغ، العقل التشغيلي للمزرعة. كيف يمكنني مساعدتك؟"
        ))
    )
    val baleghMessages: StateFlow<List<BaleghChatMessage>> = _baleghMessages.asStateFlow()

    val summaryData = combine(tasks, attendance, inventoryItems, debts, purchases, sales) { tasks, attendance, inventory, debts, purchases, sales ->
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

    fun sendMessageToBalegh(text: String) {
        if (text.isBlank()) return
        _baleghMessages.update { it + BaleghChatMessage(BaleghChatMessage.Sender.USER, text) }
        _baleghUiState.value = BaleghUiState.Processing
        viewModelScope.launch {
            try {
                val response = baleghEngine.processInput(BaleghInput(text))
                _baleghMessages.update { it + BaleghChatMessage(BaleghChatMessage.Sender.BALEGH, response.responseText, response.pendingActionProposal) }
                _baleghUiState.value = response.pendingActionProposal?.let { BaleghUiState.AwaitingConfirmation(it) } ?: BaleghUiState.Idle
            } catch (e: Exception) {
                val message = e.message ?: "غير معروف"
                _baleghUiState.value = BaleghUiState.Error(message)
                _baleghMessages.update { it + BaleghChatMessage(BaleghChatMessage.Sender.SYSTEM, "حدث خطأ: " + message) }
            }
        }
    }

    fun confirmBaleghAction(proposal: BaleghActionProposal) {
        viewModelScope.launch {
            _baleghUiState.value = BaleghUiState.Processing
            baleghEngine.executeAction(proposal).fold(
                onSuccess = { message ->
                    _baleghMessages.update { list ->
                        list.map { if (it.proposal?.id == proposal.id) it.copy(resolved = true) else it } +
                            BaleghChatMessage(BaleghChatMessage.Sender.SYSTEM, message)
                    }
                    _baleghUiState.value = BaleghUiState.Idle
                },
                onFailure = { error ->
                    val message = error.message ?: "غير معروف"
                    _baleghUiState.value = BaleghUiState.Error(message)
                    _baleghMessages.update { it + BaleghChatMessage(BaleghChatMessage.Sender.SYSTEM, "فشل التنفيذ: " + message) }
                }
            )
        }
    }

    fun cancelBaleghAction(proposal: BaleghActionProposal) {
        _baleghMessages.update { list ->
            list.map { if (it.proposal?.id == proposal.id) it.copy(resolved = false) else it } +
                BaleghChatMessage(BaleghChatMessage.Sender.SYSTEM, "تم إلغاء الإجراء.")
        }
        _baleghUiState.value = BaleghUiState.Idle
    }

    fun triggerBaleghVoice() {
        if (!baleghEngine.getSpeechProvider().isAvailable()) {
            _baleghMessages.update { it + BaleghChatMessage(BaleghChatMessage.Sender.SYSTEM, "التعرف الصوتي غير مفعّل في هذه النسخة. استخدم الكتابة حالياً.") }
        }
    }

    fun addChatMessage(message: String, isUser: Boolean) { if (isUser) sendMessageToBalegh(message) }

    fun insertLand(x: Land) = viewModelScope.launch { repository.insertLand(x) }
    fun deleteLand(x: Land) = viewModelScope.launch { repository.deleteLand(x) }
    fun insertCrop(x: Crop) = viewModelScope.launch { repository.insertCrop(x) }
    fun deleteCrop(x: Crop) = viewModelScope.launch { repository.deleteCrop(x) }
    fun insertOperation(x: Operation) = viewModelScope.launch { repository.insertOperation(x) }
    fun insertInventoryItem(x: InventoryItem) = viewModelScope.launch { repository.insertInventoryItem(x) }
    fun deleteInventoryItem(x: InventoryItem) = viewModelScope.launch { repository.deleteInventoryItem(x) }
    fun insertInventoryMovement(x: InventoryMovement) = viewModelScope.launch { repository.recordInventoryMovement(x) }
    fun insertAnimal(x: Animal) = viewModelScope.launch { repository.insertAnimal(x) }
    fun deleteAnimal(x: Animal) = viewModelScope.launch { repository.deleteAnimal(x) }
    fun insertAnimalProduction(x: AnimalProduction) = viewModelScope.launch { repository.insertAnimalProduction(x) }
    fun insertWorker(x: Worker) = viewModelScope.launch { repository.insertWorker(x) }
    fun deleteWorker(x: Worker) = viewModelScope.launch { repository.deleteWorker(x) }
    fun insertAttendance(x: Attendance) = viewModelScope.launch { repository.insertAttendance(x) }
    fun insertContact(x: Contact) = viewModelScope.launch { repository.insertContact(x) }
    fun deleteContact(x: Contact) = viewModelScope.launch { repository.deleteContact(x) }
    fun insertEquipment(x: Equipment) = viewModelScope.launch { repository.insertEquipment(x) }
    fun deleteEquipment(x: Equipment) = viewModelScope.launch { repository.deleteEquipment(x) }
    fun insertMaintenance(x: Maintenance) = viewModelScope.launch { repository.insertMaintenance(x) }
    fun insertWaterLog(x: WaterLog) = viewModelScope.launch { repository.insertWaterLog(x) }
    fun insertPurchase(x: Purchase) = viewModelScope.launch { repository.recordPurchase(x) }
    fun deletePurchase(x: Purchase) = viewModelScope.launch { repository.deletePurchase(x) }
    fun insertSale(x: Sale) = viewModelScope.launch { repository.recordSale(x) }
    fun deleteSale(x: Sale) = viewModelScope.launch { repository.deleteSale(x) }
    fun insertTreasuryTransaction(x: TreasuryTransaction) = viewModelScope.launch { repository.insertTreasuryTransaction(x) }
    fun insertDebt(x: Debt) = viewModelScope.launch { repository.insertDebt(x) }
    fun insertMeeting(x: Meeting) = viewModelScope.launch { repository.insertMeeting(x) }
    fun deleteMeeting(x: Meeting) = viewModelScope.launch { repository.deleteMeeting(x) }
    fun insertTask(x: FarmTask) = viewModelScope.launch { repository.insertTask(x) }
    fun updateTaskProgress(id: Int, progress: Int, status: String) = viewModelScope.launch { repository.updateTaskProgress(id, progress, status) }
    fun deleteTask(x: FarmTask) = viewModelScope.launch { repository.deleteTask(x) }
    fun markAllWorkersPresent(date: String) = viewModelScope.launch { workers.value.forEach { worker -> repository.insertAttendance(Attendance(workerCode = worker.code, date = date, status = "حاضر")) } }
}

data class DashboardSummary(
    val totalTasks: Int = 0, val completedTasks: Int = 0, val inProgressTasks: Int = 0,
    val pendingTasks: Int = 0, val presentWorkers: Int = 0, val lowStockCount: Int = 0,
    val activeDebtsCount: Int = 0, val monthlyRevenue: Double = 0.0, val monthlyExpenses: Double = 0.0
)
