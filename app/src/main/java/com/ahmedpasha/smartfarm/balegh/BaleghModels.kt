package com.ahmedpasha.smartfarm.balegh

data class BaleghInput(val text: String, val timestamp: Long = System.currentTimeMillis(), val source: InputSource = InputSource.TEXT)
enum class InputSource { TEXT, VOICE, AUTOMATED }
enum class InformationFactType { FACT, OBSERVATION, POSSIBILITY, SUGGESTION, USER_REQUEST, DECISION, COMMITMENT }
data class BaleghAnalysis(val summary: String, val intent: BaleghIntent, val entities: List<BaleghEntity>, val factType: InformationFactType, val confidence: Float, val clarificationNeeded: Boolean = false, val clarificationQuestion: String? = null)
data class TargetedFarmContext(
    val workers: List<com.ahmedpasha.smartfarm.data.models.Worker> = emptyList(),
    val inventoryItems: List<com.ahmedpasha.smartfarm.data.models.InventoryItem> = emptyList(),
    val lowStockItems: List<com.ahmedpasha.smartfarm.data.models.InventoryItem> = emptyList(),
    val crops: List<com.ahmedpasha.smartfarm.data.models.Crop> = emptyList(),
    val lands: List<com.ahmedpasha.smartfarm.data.models.Land> = emptyList(),
    val tasks: List<com.ahmedpasha.smartfarm.data.models.FarmTask> = emptyList(),
    val totalPurchasesMonth: Double = 0.0, val totalSalesMonth: Double = 0.0)
data class BaleghResponse(val responseText: String, val analysis: BaleghAnalysis, val pendingActionProposal: BaleghActionProposal? = null)
sealed interface BaleghUiState { data object Idle : BaleghUiState; data object Processing : BaleghUiState; data class AwaitingConfirmation(val proposal: BaleghActionProposal) : BaleghUiState; data class Error(val message: String) : BaleghUiState }
data class BaleghChatMessage(val id: String = java.util.UUID.randomUUID().toString(), val sender: Sender, val text: String, val proposal: BaleghActionProposal? = null, val resolved: Boolean? = null) { enum class Sender { USER, BALEGH, SYSTEM } }
