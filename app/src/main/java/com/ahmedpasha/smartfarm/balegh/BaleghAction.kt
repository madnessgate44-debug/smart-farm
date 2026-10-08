package com.ahmedpasha.smartfarm.balegh
enum class BaleghActionType { CREATE_TASK, CREATE_PURCHASE, CREATE_SALE, RECORD_OBSERVATION }
data class BaleghActionProposal(val id: String = java.util.UUID.randomUUID().toString(), val actionType: BaleghActionType, val description: String, val parameters: Map<String,String>, val confidence: Float, val requiresConfirmation: Boolean = true)
