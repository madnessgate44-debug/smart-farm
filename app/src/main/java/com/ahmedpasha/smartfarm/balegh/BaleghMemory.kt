package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.models.AhmedPreference
import com.ahmedpasha.smartfarm.data.repository.FarmRepository
import kotlinx.coroutines.flow.first

data class BaleghMemoryFact(
    val key: String,
    val value: String,
    val category: String,
    val confidence: Float = 1f
)

class BaleghMemory(private val repository: FarmRepository) {
    suspend fun all(): List<BaleghMemoryFact> =
        repository.allPreferences.first().map {
            BaleghMemoryFact(it.key, it.value, it.category, confidenceFrom(it.derivedInsights))
        }

    suspend fun find(key: String): BaleghMemoryFact? =
        all().firstOrNull { it.key.equals(key, true) }

    suspend fun remember(fact: BaleghMemoryFact) {
        repository.insertPreference(
            AhmedPreference(
                key = fact.key,
                value = fact.value,
                category = fact.category,
                derivedInsights = "confidence=" + fact.confidence
            )
        )
    }

    suspend fun rememberIfNew(fact: BaleghMemoryFact): Boolean {
        if (find(fact.key) != null) return false
        remember(fact)
        return true
    }

    private fun confidenceFrom(raw: String?): Float =
        raw?.substringAfter("confidence=", "")?.toFloatOrNull() ?: 1f
}
