package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.models.FarmTask
import com.ahmedpasha.smartfarm.data.models.Meeting
import com.ahmedpasha.smartfarm.data.models.Purchase
import com.ahmedpasha.smartfarm.data.models.Sale
import com.ahmedpasha.smartfarm.data.models.InventoryItem
import com.ahmedpasha.smartfarm.data.repository.FarmRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class BaleghActionExecutor(private val repository: FarmRepository) {
    private val executionMutex = Mutex()

    suspend fun execute(proposal: BaleghActionProposal): Result<String> =
        executionMutex.withLock { executeLocked(proposal) }

    private suspend fun executeLocked(proposal: BaleghActionProposal): Result<String> {
        if (!proposal.requiresConfirmation) {
            return Result.failure(IllegalStateException("Consequential Baleegh actions require confirmation."))
        }
        if (proposal.id.isBlank()) {
            return Result.failure(IllegalArgumentException("معرّف الإجراء غير صالح."))
        }

        val marker = marker(proposal.id)
        return try {
            if (alreadyExecuted(proposal.actionType, marker)) {
                return Result.success("الإجراء ده مسجل بالفعل. لم أكرر التسجيل.")
            }

            when (proposal.actionType) {
                BaleghActionType.CREATE_TASK -> {
                    val workerCode = proposal.parameters["workerCode"]?.trim().orEmpty()
                    if (workerCode.isBlank()) return Result.failure(IllegalArgumentException("العامل غير محدد."))
                    val worker = repository.allWorkers.first().firstOrNull { it.code.equals(workerCode, true) }
                        ?: return Result.failure(IllegalArgumentException("العامل ده مش موجود في سجلات المزرعة."))
                    if (worker.status != "نشط") {
                        return Result.failure(IllegalArgumentException("العامل المحدد غير نشط. راجع بياناته قبل إسناد المهمة."))
                    }
                    val taskName = proposal.parameters["taskName"]?.trim().orEmpty()
                    if (taskName.isBlank()) return Result.failure(IllegalArgumentException("وصف المهمة غير محدد."))
                    val date = requestedDate(proposal.parameters["date"])
                        ?: return Result.failure(IllegalArgumentException("التاريخ غير صالح. استخدم صيغة YYYY-MM-DD."))
                    repository.insertTask(
                        FarmTask(
                            date = date,
                            taskName = taskName,
                            workerCode = worker.code,
                            workerName = worker.name,
                            notes = marker
                        )
                    )
                    val verified = repository.allTasks.first().any { it.notes.contains(marker) }
                    if (!verified) throw IllegalStateException("تم إرسال الحفظ لكن لم أستطع التحقق من وجود المهمة في قاعدة البيانات.")
                    Result.success("تم إنشاء المهمة والتحقق من حفظها: " + taskName + " — العامل: " + worker.name + " — التاريخ: " + date + ".")
                }

                BaleghActionType.CREATE_PURCHASE -> {
                    val itemName = proposal.parameters["item"]?.trim().orEmpty()
                    if (itemName.isBlank()) return Result.failure(IllegalArgumentException("الصنف غير محدد."))
                    val amount = proposal.parameters["totalCost"]?.toDoubleOrNull()
                        ?: return Result.failure(IllegalArgumentException("قيمة الشراء غير محددة."))
                    val quantity = proposal.parameters["quantity"]?.toDoubleOrNull() ?: 1.0
                    val paid = proposal.parameters["paid"]?.toDoubleOrNull() ?: 0.0
                    if (!amount.isFinite() || amount <= 0.0) return Result.failure(IllegalArgumentException("قيمة الشراء لازم تكون أكبر من صفر."))
                    if (!quantity.isFinite() || quantity <= 0.0) return Result.failure(IllegalArgumentException("كمية الشراء لازم تكون أكبر من صفر."))
                    if (!paid.isFinite() || paid < 0.0 || paid > amount) return Result.failure(IllegalArgumentException("المبلغ المسدد لازم يكون بين صفر وإجمالي الشراء."))
                    val date = requestedDate(proposal.parameters["date"])
                        ?: return Result.failure(IllegalArgumentException("التاريخ غير صالح. استخدم صيغة YYYY-MM-DD."))
                    val inventoryItem = findInventoryItem(itemName)
                    val oldBalance = inventoryItem?.currentBalance
                    val purchase = Purchase(
                        date = date,
                        item = itemName,
                        quantity = quantity,
                        unitPrice = amount / quantity,
                        totalCost = amount,
                        paid = paid,
                        paymentMethod = proposal.parameters["paymentMethod"]?.takeIf { it.isNotBlank() } ?: "غير محدد",
                        supplier = proposal.parameters["supplier"]?.takeIf { it.isNotBlank() } ?: "غير محدد",
                        notes = "تم التسجيل عبر بليغ"
                    )
                    repository.recordPurchaseWithInventory(purchase, inventoryItem?.code, proposal.id)
                    val saved = repository.allPurchases.first().any { it.notes.contains(marker) }
                    if (!saved) throw IllegalStateException("لم أستطع التحقق من حفظ عملية الشراء.")
                    if (inventoryItem != null) {
                        val movementSaved = repository.allInventoryMovements.first().any { it.notes.contains(marker) }
                        val newBalance = repository.allInventoryItems.first().firstOrNull { it.code == inventoryItem.code }?.currentBalance
                        if (!movementSaved || newBalance == null || abs(newBalance - (oldBalance!! + quantity)) > 0.0001) {
                            throw IllegalStateException("تم تسجيل الشراء لكن تعذر التحقق من تحديث المخزون؛ راجع السجل قبل إعادة المحاولة.")
                        }
                    }
                    val stockMessage = if (inventoryItem == null) " لم يتم تعديل المخزون لأن الصنف غير مسجل بالاسم نفسه." else " وتم تحديث رصيد المخزون والتحقق منه."
                    Result.success("تم تسجيل الشراء والتحقق من حفظه: " + itemName + " بإجمالي " + amount + " جنيه، الكمية " + quantity + "، المسدد المسجل " + paid + " جنيه." + stockMessage)
                }

                BaleghActionType.CREATE_SALE -> {
                    val itemName = proposal.parameters["item"]?.trim().orEmpty()
                    if (itemName.isBlank()) return Result.failure(IllegalArgumentException("الصنف غير محدد."))
                    val amount = proposal.parameters["totalRevenue"]?.toDoubleOrNull()
                        ?: return Result.failure(IllegalArgumentException("قيمة البيع غير محددة."))
                    val quantity = proposal.parameters["quantity"]?.toDoubleOrNull() ?: 1.0
                    val received = proposal.parameters["received"]?.toDoubleOrNull() ?: 0.0
                    if (!amount.isFinite() || amount <= 0.0) return Result.failure(IllegalArgumentException("قيمة البيع لازم تكون أكبر من صفر."))
                    if (!quantity.isFinite() || quantity <= 0.0) return Result.failure(IllegalArgumentException("كمية البيع لازم تكون أكبر من صفر."))
                    if (!received.isFinite() || received < 0.0 || received > amount) return Result.failure(IllegalArgumentException("المبلغ المقبوض لازم يكون بين صفر وإجمالي البيع."))
                    val date = requestedDate(proposal.parameters["date"])
                        ?: return Result.failure(IllegalArgumentException("التاريخ غير صالح. استخدم صيغة YYYY-MM-DD."))
                    val inventoryItem = findInventoryItem(itemName)
                    val oldBalance = inventoryItem?.currentBalance
                    val sale = Sale(
                        date = date,
                        item = itemName,
                        quantity = quantity,
                        unitPrice = amount / quantity,
                        totalRevenue = amount,
                        received = received,
                        paymentMethod = proposal.parameters["paymentMethod"]?.takeIf { it.isNotBlank() } ?: "غير محدد",
                        customer = proposal.parameters["customer"]?.takeIf { it.isNotBlank() } ?: "غير محدد",
                        notes = "تم التسجيل عبر بليغ"
                    )
                    repository.recordSaleWithInventory(sale, inventoryItem?.code, proposal.id)
                    val saved = repository.allSales.first().any { it.notes.contains(marker) }
                    if (!saved) throw IllegalStateException("لم أستطع التحقق من حفظ عملية البيع.")
                    if (inventoryItem != null) {
                        val movementSaved = repository.allInventoryMovements.first().any { it.notes.contains(marker) }
                        val newBalance = repository.allInventoryItems.first().firstOrNull { it.code == inventoryItem.code }?.currentBalance
                        if (!movementSaved || newBalance == null || abs(newBalance - (oldBalance!! - quantity)) > 0.0001) {
                            throw IllegalStateException("تم تسجيل البيع لكن تعذر التحقق من تحديث المخزون؛ راجع السجل قبل إعادة المحاولة.")
                        }
                    }
                    val stockMessage = if (inventoryItem == null) " لم يتم تعديل المخزون لأن الصنف غير مسجل بالاسم نفسه." else " وتم خصم الكمية من المخزون والتحقق من الرصيد."
                    Result.success("تم تسجيل البيع والتحقق من حفظه: " + itemName + " بإجمالي " + amount + " جنيه، الكمية " + quantity + "، المقبوض المسجل " + received + " جنيه." + stockMessage)
                }

                BaleghActionType.RECORD_OBSERVATION -> {
                    val details = proposal.parameters["details"]?.trim().orEmpty()
                    if (details.isBlank()) return Result.failure(IllegalArgumentException("الملاحظة غير محددة."))
                    val date = requestedDate(proposal.parameters["date"])
                        ?: return Result.failure(IllegalArgumentException("التاريخ غير صالح. استخدم صيغة YYYY-MM-DD."))
                    repository.insertMeeting(
                        Meeting(
                            title = "ملاحظة ميدانية",
                            date = date,
                            location = proposal.parameters["location"]?.takeIf { it.isNotBlank() } ?: "المزرعة",
                            attendees = proposal.parameters["attendees"]?.takeIf { it.isNotBlank() } ?: "أحمد",
                            agenda = details,
                            notes = details + "\n" + marker
                        )
                    )
                    val verified = repository.allMeetings.first().any { it.notes.contains(marker) }
                    if (!verified) throw IllegalStateException("لم أستطع التحقق من حفظ الملاحظة.")
                    Result.success("تم تسجيل الملاحظة الميدانية والتحقق من حفظها.")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun alreadyExecuted(type: BaleghActionType, marker: String): Boolean = when (type) {
        BaleghActionType.CREATE_TASK -> repository.allTasks.first().any { it.notes.contains(marker) }
        BaleghActionType.CREATE_PURCHASE -> repository.allPurchases.first().any { it.notes.contains(marker) }
        BaleghActionType.CREATE_SALE -> repository.allSales.first().any { it.notes.contains(marker) }
        BaleghActionType.RECORD_OBSERVATION -> repository.allMeetings.first().any { it.notes.contains(marker) }
    }

    private suspend fun findInventoryItem(rawName: String): InventoryItem? {
        val items = repository.allInventoryItems.first()
        items.firstOrNull { it.code.equals(rawName, true) }?.let { return it }
        val matches = items.filter { it.name.equals(rawName, true) }
        if (matches.size > 1) throw IllegalArgumentException("في أكتر من صنف بنفس الاسم. حدد كود الصنف لتجنب تعديل المخزون الخطأ.")
        return matches.singleOrNull()
    }

    private fun requestedDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return today()
        return try {
            LocalDate.parse(raw).toString()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun marker(id: String) = "BaleeghActionId=" + id
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
