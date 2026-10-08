package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.models.InventoryItem
import com.ahmedpasha.smartfarm.data.models.Worker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalBaleghUnderstandingProviderTest {
    private val provider = LocalBaleghUnderstandingProvider()
    @Test fun emptyInputIsUnknown() = runBlocking {
        val result = provider.analyze(BaleghInput(""), TargetedFarmContext())
        assertEquals(BaleghIntent.UNKNOWN, result.intent); assertEquals(0f, result.confidence)
    }
    @Test fun inventoryQueryIsReadOnlyIntent() = runBlocking {
        val item = InventoryItem("I1", "سماد", "مستلزمات", "شكارة", 10.0, 2.0, 5.0, "المخزن")
        val result = provider.analyze(BaleghInput("إيه الأصناف الناقصة في المخزن؟"), TargetedFarmContext(inventoryItems = listOf(item), lowStockItems = listOf(item)))
        assertEquals(BaleghIntent.INVENTORY_QUERY, result.intent)
    }
    @Test fun duplicateWorkerNamesRequireClarification() = runBlocking {
        val workers = listOf(Worker("W1","محمد أحمد","عامل",150.0,"نشط","2024-01-01"), Worker("W2","محمد علي","سائق",200.0,"نشط","2024-01-01"))
        val result = provider.analyze(BaleghInput("اعمل مهمة لمحمد يجيب الطلمبة بكرة"), TargetedFarmContext(workers = workers))
        assertTrue(result.clarificationNeeded)
    }
    @Test fun purchaseExtractsRequiredEntities() = runBlocking {
        val item = InventoryItem("I1","سماد","مستلزمات","شكارة",10.0,2.0,5.0,"المخزن")
        val result = provider.analyze(BaleghInput("اشتريت سماد بـ 500 جنيه"), TargetedFarmContext(inventoryItems = listOf(item)))
        assertEquals(BaleghIntent.PURCHASE_ACTION, result.intent)
        assertTrue(result.entities.any { it.type == EntityType.AMOUNT }); assertTrue(result.entities.any { it.type == EntityType.INVENTORY_ITEM })
    }
}
