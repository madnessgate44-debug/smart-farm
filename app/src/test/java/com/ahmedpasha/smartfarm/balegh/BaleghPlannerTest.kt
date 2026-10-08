package com.ahmedpasha.smartfarm.balegh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BaleghPlannerTest {
    @Test
    fun commitmentWithResolvedWorkerProducesConfirmedTaskPlan() {
        val worker = BaleghEntity(EntityType.WORKER, "محمد", "محمد", 1f, "W1")
        val deadline = BaleghEntity(EntityType.DEADLINE, "بكرة", "2030-01-02")
        val analysis = BaleghAnalysis(
            summary = "محمد هيجيب الطلمبة بكرة",
            intent = BaleghIntent.COMMITMENT,
            entities = listOf(worker, deadline),
            factType = InformationFactType.COMMITMENT,
            confidence = 0.9f
        )
        val plan = BaleghPlanner().plan(BaleghInput("محمد هيجيب الطلمبة بكرة"), analysis, BaleghFarmState())
        assertEquals(1, plan.steps.size)
        assertNotNull(plan.steps.first().action)
        assertEquals(true, plan.requiresConfirmation)
    }
}
