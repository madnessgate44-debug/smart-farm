package com.ahmedpasha.smartfarm.balegh
enum class EntityType { LAND, CROP, WORKER, INVENTORY_ITEM, TASK, AMOUNT, QUANTITY, DATE, DEADLINE, PERSON, SUPPLIER, CUSTOMER, PROBLEM }
data class BaleghEntity(val type: EntityType, val rawValue: String, val normalizedValue: String = rawValue, val confidence: Float = 1f, val resolvedCode: String? = null)
