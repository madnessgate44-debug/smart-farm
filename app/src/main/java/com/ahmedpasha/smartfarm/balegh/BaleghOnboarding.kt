package com.ahmedpasha.smartfarm.balegh

class BaleghOnboarding(private val memory: BaleghMemory) {
    suspend fun handle(text: String): String? {
        val value = text.trim()
        val completed = memory.find("onboarding.completed")?.value == "true"
        if (completed) return null

        if (value.isBlank()) return "خلينا نبدأ بتعريف بليغ بالمزرعة. اسم المزرعة إيه؟"
        if (isSkip(value)) {
            return skipCurrent()
        }

        return when {
            memory.find("farm.name") == null -> {
                memory.rememberIfNew(BaleghMemoryFact("farm.name", value, "farm_identity"))
                "تمام. المزرعة اسمها " + value + ". فين موقع المزرعة؟"
            }
            memory.find("farm.location") == null -> {
                memory.rememberIfNew(BaleghMemoryFact("farm.location", value, "farm_identity"))
                "تمام. عندنا الموقع. كام أرض أو قطعة أساسية عندك؟"
            }
            memory.find("farm.land_count") == null -> {
                val count = firstNumber(value)
                if (count == null) return "عايز العدد التقريبي للأراضي أو القطع، مثلاً: 5."
                memory.rememberIfNew(BaleghMemoryFact("farm.land_count", count.toString(), "farm_scope"))
                "تمام. كام عامل شغالين في المزرعة تقريباً؟"
            }
            memory.find("farm.worker_count") == null -> {
                val count = firstNumber(value)
                if (count == null) return "عايز عدد العمال التقريبي، مثلاً: 12."
                memory.rememberIfNew(BaleghMemoryFact("farm.worker_count", count.toString(), "farm_scope"))
                "إيه أهم المحاصيل الموجودة حالياً؟ قولهم لي بطريقتك، وممكن تقول أكتر من محصول."
            }
            memory.find("farm.crops_description") == null -> {
                memory.rememberIfNew(BaleghMemoryFact("farm.crops_description", value, "farm_scope"))
                "ممتاز. عندك حيوانات أو مواشي؟ ولو مفيش قول لي مفيش."
            }
            memory.find("farm.livestock_description") == null -> {
                memory.rememberIfNew(BaleghMemoryFact("farm.livestock_description", value, "farm_scope"))
                "آخر حاجة في البداية: إيه أهم الأصناف الموجودة في المخزن حالياً؟ قول الأصناف والكمية لو تعرف."
            }
            memory.find("farm.inventory_description") == null -> {
                memory.rememberIfNew(BaleghMemoryFact("farm.inventory_description", value, "farm_scope"))
                memory.rememberIfNew(BaleghMemoryFact("onboarding.completed", "true", "system"))
                "تمام. عرفت نطاق المزرعة الأساسي. من هنا تقدر تتكلم معايا بشكل طبيعي، وأنا هستخدم بيانات المزرعة المسجلة مع المعلومات اللي حفظناها."
            }
            else -> {
                memory.rememberIfNew(BaleghMemoryFact("onboarding.completed", "true", "system"))
                null
            }
        }
    }

    private suspend fun skipCurrent(): String {
        return when {
            memory.find("farm.name") == null -> "تمام، نقدر نكمل بعدين. لكن قبل أي تشغيل حقيقي، هحتاج اسم المزرعة. أقدر أساعدك الآن في اختبار بليغ."
            memory.find("farm.location") == null -> "تمام. هنسيب الموقع لوقت لاحق. كام أرض أو قطعة أساسية عندك؟"
            memory.find("farm.land_count") == null -> "تمام. هنكمل بدون العدد حالياً. كام عامل شغالين تقريباً؟"
            memory.find("farm.worker_count") == null -> "تمام. هنكمل بدون العدد حالياً. إيه أهم المحاصيل الموجودة؟"
            memory.find("farm.crops_description") == null -> "تمام. هنكمل بدون المحاصيل الآن. عندك حيوانات أو مواشي؟"
            memory.find("farm.livestock_description") == null -> "تمام. وآخر سؤال: إيه أهم أصناف المخزن حالياً؟"
            memory.find("farm.inventory_description") == null -> {
                memory.rememberIfNew(BaleghMemoryFact("onboarding.completed", "true", "system"))
                "تمام. خلصنا الإعداد الأساسي."
            }
            else -> null
        }
    }

    private fun isSkip(value: String): Boolean =
        listOf("skip", "تخطى", "عدي", "بعد كده", "بعدين", "مش دلوقتي", "لاحقا", "لاحقاً").any { value.equals(it, true) }

    private fun firstNumber(value: String): Int? =
        Regex("\d+").find(value)?.value?.toIntOrNull()
}
