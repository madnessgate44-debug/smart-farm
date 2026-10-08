package com.ahmedpasha.smartfarm.balegh

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LocalBaleghUnderstandingProvider : BaleghUnderstandingProvider {
    override suspend fun analyze(input: BaleghInput, context: TargetedFarmContext): BaleghAnalysis {
        val text = input.text.trim()
        if (text.isBlank()) return BaleghAnalysis("محتوى فارغ", BaleghIntent.UNKNOWN, emptyList(), InformationFactType.OBSERVATION, 0f)

        val entities = mutableListOf<BaleghEntity>()
        extractAmount(text)?.let(entities::add)
        extractQuantity(text)?.let(entities::add)
        extractDate(text)?.let(entities::add)

        val workerMatches = context.workers.filter { worker ->
            text.contains(worker.name, true) || worker.name.split(" ").any { it.length > 2 && text.contains(it, true) }
        }
        workerMatches.forEach { worker ->
            entities += BaleghEntity(EntityType.WORKER, worker.name, worker.name, 1f, worker.code)
        }

        context.inventoryItems.filter { text.contains(it.name, true) }.forEach { item ->
            entities += BaleghEntity(EntityType.INVENTORY_ITEM, item.name, item.name, 1f, item.code)
        }
        context.crops.filter { text.contains(it.crop, true) || text.contains(it.code, true) }.forEach { crop ->
            entities += BaleghEntity(EntityType.CROP, crop.crop, crop.crop, 1f, crop.code)
        }
        context.lands.filter { text.contains(it.name, true) || text.contains(it.code, true) }.forEach { land ->
            entities += BaleghEntity(EntityType.LAND, land.name, land.name, 1f, land.code)
        }

        val intent: BaleghIntent
        val factType: InformationFactType
        when {
            hasAny(text, "حالة المزرعة", "وضع المزرعة", "حالة المزرعه", "الوضع العام") -> { intent = BaleghIntent.FARM_STATUS; factType = InformationFactType.USER_REQUEST }
            hasAny(text, "ناقص", "المخزن", "المخازن", "المخزون") -> { intent = BaleghIntent.INVENTORY_QUERY; factType = InformationFactType.USER_REQUEST }
            hasAny(text, "المصاريف", "المصروفات", "التكلفة", "الخزينة", "المبيعات", "المشتريات") -> { intent = BaleghIntent.FINANCE_QUERY; factType = InformationFactType.USER_REQUEST }
            hasAny(text, "محصول", "المحاصيل", "زراعة", "زرع") -> { intent = BaleghIntent.CROP_QUERY; factType = InformationFactType.USER_REQUEST }
            hasAny(text, "أرض", "الأرض", "قطعة", "القطع") -> { intent = BaleghIntent.LAND_QUERY; factType = InformationFactType.USER_REQUEST }
            hasAny(text, "اعمل مهمة", "سجل مهمة", "كلف", "مهمة لـ", "مهمة ل") -> { intent = BaleghIntent.CREATE_TASK; factType = InformationFactType.USER_REQUEST }
            hasAny(text, "هيجيب", "هيعمل", "وعد", "سيلتزم") -> { intent = BaleghIntent.COMMITMENT; factType = InformationFactType.COMMITMENT }
            hasAny(text, "اشتريت", "شراء", "اتشترى") -> { intent = BaleghIntent.PURCHASE_ACTION; factType = InformationFactType.DECISION }
            hasAny(text, "بعنا", "بيعنا", "بيع", "اتباع") -> { intent = BaleghIntent.SALE_ACTION; factType = InformationFactType.DECISION }
            hasAny(text, "مشكلة", "عطل", "مش شغال", "محتاجة صيانة", "محتاج ري") -> { intent = BaleghIntent.PROBLEM_REPORT; factType = InformationFactType.OBSERVATION }
            hasAny(text, "عامل", "عمال", "مين اشتغل", "مين حضر", "الحضور") -> { intent = BaleghIntent.WORKER_QUERY; factType = InformationFactType.USER_REQUEST }
            else -> { intent = BaleghIntent.GENERAL_QUERY; factType = InformationFactType.USER_REQUEST }
        }

        val ambiguousWorker = workerMatches.size > 1 &&
            intent in setOf(BaleghIntent.CREATE_TASK, BaleghIntent.COMMITMENT, BaleghIntent.WORKER_QUERY)

        return BaleghAnalysis(
            summary = text,
            intent = intent,
            entities = entities,
            factType = factType,
            confidence = if (ambiguousWorker) 0.55f else 0.85f,
            clarificationNeeded = ambiguousWorker,
            clarificationQuestion = if (ambiguousWorker) "في أكتر من عامل مطابق. تقصد مين بالضبط؟" else null
        )
    }

    private fun hasAny(text: String, vararg values: String) = values.any { text.contains(it, true) }

    private fun extractAmount(text: String): BaleghEntity? {
        val match = Regex("""(d+(?:[.,]d+)?)s*(جنيه|ج|ألف|الف)""").find(text) ?: return null
        val number = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
        val amount = if (match.groupValues[2] == "ألف" || match.groupValues[2] == "الف") number * 1000 else number
        return BaleghEntity(EntityType.AMOUNT, match.value, amount.toString())
    }

    private fun extractQuantity(text: String): BaleghEntity? {
        val match = Regex("""(d+(?:[.,]d+)?)s*(كيلو|كجم|طن|شكارة|شكاير|قطعة|قطع|لتر|لترات)""").find(text) ?: return null
        return BaleghEntity(EntityType.QUANTITY, match.value, match.groupValues[1].replace(",", "."))
    }

    private fun extractDate(text: String): BaleghEntity? {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return when {
            hasAny(text, "بعد بكرة") -> { calendar.add(Calendar.DAY_OF_YEAR, 2); BaleghEntity(EntityType.DEADLINE, "بعد بكرة", format.format(calendar.time)) }
            hasAny(text, "بكرة", "غدا", "غداً") -> { calendar.add(Calendar.DAY_OF_YEAR, 1); BaleghEntity(EntityType.DEADLINE, "بكرة", format.format(calendar.time)) }
            hasAny(text, "اليوم", "النهارده", "النهاردة") -> BaleghEntity(EntityType.DATE, "اليوم", format.format(calendar.time))
            else -> null
        }
    }
}
