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
    private val executor = BaleghActionExecutor(repository)

    suspend fun processInput(input: BaleghInput): BaleghResponse {
        val context = contextProvider.getContextFor(input.text)
        val analysis = understandingProvider.analyze(input, context)
        if (analysis.clarificationNeeded) return BaleghResponse(analysis.clarificationQuestion ?: "محتاج توضيح بسيط.", analysis)
        val result = when (analysis.intent) {
            BaleghIntent.INVENTORY_QUERY -> {
                val text = if (context.lowStockItems.isEmpty()) "المخزن لا يحتوي حالياً على أصناف عند الحد الأدنى أو أقل."
                else "الأصناف الناقصة: " + context.lowStockItems.joinToString("، ") { item -> item.name + " (" + item.currentBalance + " " + item.unit + ")" }
                ResultText(text)
            }
            BaleghIntent.FINANCE_QUERY -> ResultText("هذا الشهر: المصروفات " + context.totalPurchasesMonth + " جنيه، والمبيعات " + context.totalSalesMonth + " جنيه.")
            BaleghIntent.WORKER_QUERY -> ResultText(if (context.workers.isEmpty()) "لا توجد بيانات عمال متاحة لهذا الاستفسار." else context.workers.joinToString("، ") { worker -> worker.name + " — " + worker.job })
            BaleghIntent.CREATE_TASK, BaleghIntent.COMMITMENT -> createTask(input, analysis)
            BaleghIntent.PURCHASE_ACTION -> createPurchase(analysis)
            BaleghIntent.SALE_ACTION -> createSale(analysis)
            BaleghIntent.PROBLEM_REPORT -> ResultProposal("فهمت أنها ملاحظة ميدانية. راجعها ثم اضغط «تنفيذ».", BaleghActionProposal(BaleghActionType.RECORD_OBSERVATION, "حفظ الملاحظة: " + input.text, mapOf("details" to input.text), analysis.confidence))
            else -> ResultText("فهمت الطلب، لكن لا يوجد إجراء آمن ومطابق له في إصدار بليغ الحالي.")
        }
        return BaleghResponse(result.message, analysis, result.proposal)
    }

    suspend fun executeAction(proposal: BaleghActionProposal): Result<String> = executor.execute(proposal)
    fun getSpeechProvider(): BaleghSpeechProvider = speechProvider

    private fun createTask(input: BaleghInput, analysis: BaleghAnalysis): ResultProposal {
        val worker = analysis.entities.filter { it.type == EntityType.WORKER }
        if (worker.size != 1) return ResultProposal("محتاج أعرف العامل المقصود قبل إنشاء المهمة.", null)
        val w = worker.single()
        val date = analysis.entities.firstOrNull { it.type == EntityType.DEADLINE }?.normalizedValue ?: today()
        val taskName = input.text.replace(Regex("(?i)اعمل مهمة|سجل مهمة|كلف"), "").replace("بكرة", "").replace("غدا", "").replace("غداً", "").trim()
        if (taskName.isBlank()) return ResultProposal("ما هي تفاصيل المهمة؟", null)
        return ResultProposal("فهمت المهمة. راجع التفاصيل ثم اضغط «تنفيذ».",
            BaleghActionProposal(BaleghActionType.CREATE_TASK, "إضافة مهمة للعامل " + w.rawValue + ": " + taskName + " — " + date,
                mapOf("workerCode" to (w.resolvedCode ?: ""), "workerName" to w.rawValue, "taskName" to taskName, "date" to date), analysis.confidence))
    }

    private fun createPurchase(analysis: BaleghAnalysis): ResultProposal {
        val item = analysis.entities.firstOrNull { it.type == EntityType.INVENTORY_ITEM }
        val amount = analysis.entities.firstOrNull { it.type == EntityType.AMOUNT }
        if (item == null || amount == null) return ResultProposal("لتسجيل الشراء أحتاج اسم الصنف وقيمته.", null)
        val quantity = analysis.entities.firstOrNull { it.type == EntityType.QUANTITY }?.normalizedValue ?: "1"
        return ResultProposal("سأضيف عملية الشراء بهذه البيانات. راجعها ثم اضغط «تنفيذ».",
            BaleghActionProposal(BaleghActionType.CREATE_PURCHASE, "شراء " + item.rawValue + " بقيمة " + amount.normalizedValue + " جنيه، كمية " + quantity,
                mapOf("item" to item.rawValue, "totalCost" to amount.normalizedValue, "quantity" to quantity), analysis.confidence))
    }

    private fun createSale(analysis: BaleghAnalysis): ResultProposal {
        val item = analysis.entities.firstOrNull { it.type == EntityType.CROP }
        val amount = analysis.entities.firstOrNull { it.type == EntityType.AMOUNT }
        if (item == null || amount == null) return ResultProposal("لتسجيل البيع أحتاج اسم المحصول وقيمته.", null)
        val quantity = analysis.entities.firstOrNull { it.type == EntityType.QUANTITY }?.normalizedValue ?: "1"
        return ResultProposal("سأضيف عملية البيع بهذه البيانات. راجعها ثم اضغط «تنفيذ».",
            BaleghActionProposal(BaleghActionType.CREATE_SALE, "بيع " + item.rawValue + " بقيمة " + amount.normalizedValue + " جنيه، كمية " + quantity,
                mapOf("item" to item.rawValue, "totalRevenue" to amount.normalizedValue, "quantity" to quantity), analysis.confidence))
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private data class ResultText(val message: String, val proposal: BaleghActionProposal? = null)
    private data class ResultProposal(val message: String, val proposal: BaleghActionProposal?)
}
