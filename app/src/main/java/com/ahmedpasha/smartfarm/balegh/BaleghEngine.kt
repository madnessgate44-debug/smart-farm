package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.repository.FarmRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BaleghEngine(
    repository: FarmRepository,
    private val understandingProvider: BaleghUnderstandingProvider = LocalBaleghUnderstandingProvider(),
    private val speechProvider: BaleghSpeechProvider = UnconfiguredSpeechProvider()
) {
    private val contextProvider = BaleghContextProvider(repository)
    private val farmStateProvider = BaleghFarmStateProvider(repository)
    private val memory = BaleghMemory(repository)
    private val planner = BaleghPlanner()
    private val executor = BaleghActionExecutor(repository)

    suspend fun processInput(input: BaleghInput): BaleghResponse {
        val context = contextProvider.getContextFor(input.text)
        val analysis = understandingProvider.analyze(input, context)

        if (analysis.clarificationNeeded) {
            val message = analysis.clarificationQuestion ?: "محتاج توضيح بسيط."
            speakIfVoice(input, message)
            return BaleghResponse(message, analysis)
        }

        val state = farmStateProvider.loadFor(analysis.intent, input.text)
        val plan = planner.plan(input, analysis, state)
        val response = when (analysis.intent) {
            BaleghIntent.INVENTORY_QUERY -> inventoryResponse(state)
            BaleghIntent.FINANCE_QUERY -> financeResponse(state)
            BaleghIntent.WORKER_QUERY -> workerResponse(state)
            BaleghIntent.CROP_QUERY -> cropResponse(state)
            BaleghIntent.LAND_QUERY -> landResponse(state)
            BaleghIntent.FARM_STATUS -> farmStatusResponse(state)
            BaleghIntent.CREATE_TASK, BaleghIntent.COMMITMENT -> {
                val planned = plan.steps.firstOrNull()?.action
                if (planned == null) ResultText("محتاج أعرف العامل أو تفاصيل المهمة قبل ما أسجلها.")
                else ResultProposal("فهمت الطلب. راجع التفاصيل ثم أكد التنفيذ.", planned)
            }
            BaleghIntent.PURCHASE_ACTION -> createPurchase(analysis)
            BaleghIntent.SALE_ACTION -> createSale(analysis)
            BaleghIntent.PROBLEM_REPORT -> ResultProposal(
                "سجلت فهمي للمشكلة. راجعها قبل الحفظ.",
                BaleghActionProposal(
                    BaleghActionType.RECORD_OBSERVATION,
                    "حفظ الملاحظة: " + input.text,
                    mapOf("details" to input.text),
                    analysis.confidence
                )
            )
            BaleghIntent.GENERAL_QUERY -> generalResponse(state)
            else -> ResultText("فهمت الطلب، لكن ما زال لا يوجد إجراء مطابق وآمن له.")
        }

        val finalResponse = BaleghResponse(response.message, analysis, response.proposal)
        speakIfVoice(input, finalResponse.responseText)
        return finalResponse
    }

    suspend fun executeAction(proposal: BaleghActionProposal): Result<String> = executor.execute(proposal)

    fun getSpeechProvider(): BaleghSpeechProvider = speechProvider

    suspend fun memoryFacts(): List<BaleghMemoryFact> = memory.all()

    private fun inventoryResponse(state: BaleghFarmState): ResultText {
        if (state.inventory.isEmpty()) return ResultText("لا توجد أصناف مخزون مسجلة حالياً.")
        val low = state.lowStock
        return if (low.isEmpty()) {
            ResultText("المخزون المسجل حالياً " + state.inventory.size + " أصناف، ولا يوجد صنف عند الحد الأدنى أو أقل.")
        } else {
            ResultText("الأصناف التي وصلت للحد الأدنى أو أقل: " + low.joinToString("، ") {
                it.name + " (" + it.currentBalance + " " + it.unit + ")"
            })
        }
    }

    private fun financeResponse(state: BaleghFarmState): ResultText {
        val purchases = state.purchases.sumOf { it.totalCost }
        val sales = state.sales.sumOf { it.totalRevenue }
        return ResultText("إجمالي المشتريات المسجلة: " + purchases + " جنيه. إجمالي المبيعات المسجلة: " + sales + " جنيه.")
    }

    private fun workerResponse(state: BaleghFarmState): ResultText {
        if (state.workers.isEmpty()) return ResultText("لا توجد بيانات عمال مسجلة.")
        val today = state.today()
        val present = state.attendance.count { it.date == today && it.status == "حاضر" }
        return ResultText("عدد العمال المسجلين " + state.workers.size + "، والحضور المسجل اليوم " + present + ".")
    }

    private fun cropResponse(state: BaleghFarmState): ResultText {
        if (state.crops.isEmpty()) return ResultText("لا توجد محاصيل مسجلة.")
        return ResultText("المحاصيل المسجلة: " + state.crops.joinToString("، ") { it.crop + " في " + it.landCode })
    }

    private fun landResponse(state: BaleghFarmState): ResultText {
        if (state.lands.isEmpty()) return ResultText("لا توجد أراضٍ مسجلة.")
        return ResultText("الأراضي المسجلة: " + state.lands.joinToString("، ") { it.name + " (" + it.area + ")" })
    }

    private fun farmStatusResponse(state: BaleghFarmState): ResultText =
        ResultText("حالياً: " + state.lands.size + " أراضٍ، " + state.crops.size + " محاصيل، " + state.workers.size + " عمال، " + state.inventory.size + " أصناف مخزون، و" + state.tasks.count { it.status != "مكتمل" } + " مهام غير مكتملة.")

    private fun generalResponse(state: BaleghFarmState): ResultText {
        val memoryCount = memory.all().size
        return ResultText("أنا متصل ببيانات المزرعة الحالية. عندي " + memoryCount + " معلومة محفوظة في ذاكرة بليغ. قل لي ما الذي تريد فحصه أو تنفيذه.")
    }

    private fun createPurchase(analysis: BaleghAnalysis): ResultProposal {
        val item = analysis.entities.firstOrNull { it.type == EntityType.INVENTORY_ITEM }
        val amount = analysis.entities.firstOrNull { it.type == EntityType.AMOUNT }
        if (item == null || amount == null) return ResultProposal("لتسجيل الشراء أحتاج اسم الصنف وقيمته.", null)
        val quantity = analysis.entities.firstOrNull { it.type == EntityType.QUANTITY }?.normalizedValue ?: "1"
        return ResultProposal(
            "سأضيف عملية الشراء بهذه البيانات. راجعها ثم أكد التنفيذ.",
            BaleghActionProposal(
                BaleghActionType.CREATE_PURCHASE,
                "شراء " + item.rawValue + " بقيمة " + amount.normalizedValue + " جنيه، كمية " + quantity,
                mapOf("item" to item.rawValue, "totalCost" to amount.normalizedValue, "quantity" to quantity),
                analysis.confidence
            )
        )
    }

    private fun createSale(analysis: BaleghAnalysis): ResultProposal {
        val item = analysis.entities.firstOrNull { it.type == EntityType.CROP }
        val amount = analysis.entities.firstOrNull { it.type == EntityType.AMOUNT }
        if (item == null || amount == null) return ResultProposal("لتسجيل البيع أحتاج اسم المحصول وقيمته.", null)
        val quantity = analysis.entities.firstOrNull { it.type == EntityType.QUANTITY }?.normalizedValue ?: "1"
        return ResultProposal(
            "سأضيف عملية البيع بهذه البيانات. راجعها ثم أكد التنفيذ.",
            BaleghActionProposal(
                BaleghActionType.CREATE_SALE,
                "بيع " + item.rawValue + " بقيمة " + amount.normalizedValue + " جنيه، كمية " + quantity,
                mapOf("item" to item.rawValue, "totalRevenue" to amount.normalizedValue, "quantity" to quantity),
                analysis.confidence
            )
        )
    }

    private fun speakIfVoice(input: BaleghInput, text: String) {
        if (input.source == InputSource.VOICE) speechProvider.speak(text)
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private data class ResultText(val message: String, val proposal: BaleghActionProposal? = null)
    private data class ResultProposal(val message: String, val proposal: BaleghActionProposal?)
}
