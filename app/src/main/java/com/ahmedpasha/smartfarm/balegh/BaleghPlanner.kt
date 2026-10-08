package com.ahmedpasha.smartfarm.balegh

data class BaleghPlanStep(
    val id: String,
    val description: String,
    val action: BaleghActionProposal? = null,
    val requiresConfirmation: Boolean = action?.requiresConfirmation ?: false
)

data class BaleghPlan(
    val summary: String,
    val steps: List<BaleghPlanStep>,
    val confidence: Float,
    val clarification: String? = null
) {
    val requiresConfirmation: Boolean
        get() = steps.any { it.requiresConfirmation }
}

class BaleghPlanner {
    fun plan(input: BaleghInput, analysis: BaleghAnalysis, state: BaleghFarmState): BaleghPlan {
        if (analysis.clarificationNeeded) {
            return BaleghPlan(analysis.summary, emptyList(), analysis.confidence, analysis.clarificationQuestion)
        }

        val steps = when (analysis.intent) {
            BaleghIntent.CREATE_TASK, BaleghIntent.COMMITMENT -> {
                val worker = analysis.entities.firstOrNull { it.type == EntityType.WORKER }
                val deadline = analysis.entities.firstOrNull { it.type == EntityType.DEADLINE }
                if (worker?.resolvedCode.isNullOrBlank()) emptyList()
                else listOf(
                    BaleghPlanStep(
                        id = "create-task",
                        description = "إنشاء مهمة مرتبطة بالعامل " + worker.rawValue,
                        action = BaleghActionProposal(
                            actionType = BaleghActionType.CREATE_TASK,
                            description = "إنشاء مهمة من الطلب: " + input.text,
                            parameters = mapOf(
                                "workerCode" to worker.resolvedCode.orEmpty(),
                                "workerName" to worker.rawValue,
                                "taskName" to input.text,
                                "date" to (deadline?.normalizedValue ?: state.today())
                            ),
                            confidence = analysis.confidence,
                            requiresConfirmation = true
                        )
                    )
                )
            }
            else -> emptyList()
        }

        return BaleghPlan(analysis.summary, steps, analysis.confidence)
    }
}
