package com.ahmedpasha.smartfarm.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============ LANDS ============
@Entity(tableName = "lands")
data class Land(
    @PrimaryKey val code: String,
    val name: String,
    val area: Double,
    val location: String,
    val soilType: String,
    val status: String = "نشطة",
    val notes: String = ""
)

// ============ CROPS ============
@Entity(tableName = "crops")
data class Crop(
    @PrimaryKey val code: String,
    val landCode: String,
    val crop: String,
    val season: String,
    val area: Double,
    val expectedProduction: Double,
    val actualProduction: Double = 0.0,
    val plantingDate: String,
    val harvestDate: String = "",
    val status: String = "نمو",
    val notes: String = ""
)

// ============ OPERATIONS ============
@Entity(tableName = "operations")
data class Operation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val landCode: String,
    val cropCode: String,
    val operationType: String,
    val cost: Double,
    val workerCode: String,
    val notes: String = ""
)

// ============ INVENTORY ============
@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val code: String,
    val name: String,
    val category: String,
    val unit: String,
    val initialBalance: Double,
    val currentBalance: Double,
    val minThreshold: Double,
    val location: String,
    val notes: String = ""
)

@Entity(tableName = "inventory_movements")
data class InventoryMovement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemCode: String,
    val date: String,
    val movementType: String,
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double,
    val notes: String = ""
)

// ============ ANIMALS ============
@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey val code: String,
    val type: String,
    val breed: String,
    val age: Double,
    val status: String,
    val location: String,
    val notes: String = ""
)

@Entity(tableName = "animal_production")
data class AnimalProduction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalCode: String,
    val date: String,
    val productionType: String,
    val quantity: Double,
    val unit: String,
    val revenue: Double,
    val notes: String = ""
)

// ============ WORKERS ============
@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey val code: String,
    val name: String,
    val job: String,
    val dailyRate: Double,
    val status: String = "نشط",
    val joinDate: String,
    val phone: String = "",
    val notes: String = ""
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerCode: String,
    val date: String,
    val status: String,
    val delayHours: Double = 0.0,
    val bonus: Double = 0.0,
    val deduction: Double = 0.0,
    val notes: String = ""
)

// ============ CONTACTS ============
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val code: String,
    val name: String,
    val phone: String,
    val category: String,
    val company: String = "",
    val notes: String = ""
)

// ============ EQUIPMENT ============
@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey val code: String,
    val name: String,
    val status: String,
    val lastMaintenance: String,
    val purchasePrice: Double,
    val nextMaintenanceDate: String,
    val notes: String = ""
)

@Entity(tableName = "maintenance")
data class Maintenance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equipmentCode: String,
    val date: String,
    val cost: Double,
    val details: String,
    val provider: String
)

// ============ WATER ============
@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val landCode: String,
    val source: String,
    val durationHours: Double,
    val cost: Double,
    val notes: String = ""
)

// ============ FINANCIALS ============
@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val item: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalCost: Double,
    val paid: Double,
    val paymentMethod: String,
    val supplier: String,
    val notes: String = ""
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val item: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalRevenue: Double,
    val received: Double,
    val paymentMethod: String,
    val customer: String,
    val notes: String = ""
)

@Entity(tableName = "treasury_transactions")
data class TreasuryTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val transactionType: String,
    val amount: Double,
    val category: String,
    val description: String
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val contactName: String,
    val debtType: String,
    val amount: Double,
    val dueDate: String,
    val status: String,
    val notes: String = ""
)

// ============ MEETINGS ============
@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val location: String,
    val attendees: String,
    val agenda: String,
    val notes: String,
    val audioUri: String? = null,
    val voiceNotes: String? = null
)

// ============ PREFERENCES ============
@Entity(tableName = "ahmed_preferences")
data class AhmedPreference(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val value: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val derivedInsights: String? = null
)

// ============ TASKS ============
@Entity(tableName = "farm_tasks")
data class FarmTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val taskName: String,
    val cropCode: String = "",
    val cropName: String = "",
    val workerCode: String,
    val workerName: String,
    val priority: String = "متوسطة",
    val status: String = "قيد الانتظار",
    val progress: Int = 0,
    val notes: String = ""
)