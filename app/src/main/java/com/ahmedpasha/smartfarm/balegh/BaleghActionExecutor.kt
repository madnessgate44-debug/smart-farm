package com.ahmedpasha.smartfarm.balegh

import com.ahmedpasha.smartfarm.data.models.FarmTask
import com.ahmedpasha.smartfarm.data.models.Meeting
import com.ahmedpasha.smartfarm.data.models.Purchase
import com.ahmedpasha.smartfarm.data.models.Sale
import com.ahmedpasha.smartfarm.data.repository.FarmRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BaleghActionExecutor(private val repository: FarmRepository) {
    suspend fun execute(proposal: BaleghActionProposal): Result<String> {
        if (!proposal.requiresConfirmation) {
            return Result.failure(IllegalStateException("Consequential Baleegh actions require confirmation."))
        }
        return try {
            when (proposal.actionType) {
                BaleghActionType.CREATE_TASK -> {
                    val workerCode = proposal.parameters["workerCode"] ?: return Result.failure(IllegalArgumentException("العامل غير محدد."))
                    val workerName = proposal.parameters["workerName"] ?: return Result.failure(IllegalArgumentException("اسم العامل غير محدد."))
                    val taskName = proposal.parameters["taskName"] ?: return Result.failure(IllegalArgumentException("وصف المهمة غير محدد."))
                    repository.insertTask(
                        FarmTask(
                            date = proposal.parameters["date"] ?: today(),
                            taskName = taskName,
                            workerCode = workerCode,
                            workerName = workerName
                        )
                    )
                    Result.success("تم إنشاء المهمة: " + taskName)
                }
                BaleghActionType.CREATE_PURCHASE -> {
                    val item = proposal.parameters["item"] ?: return Result.failure(IllegalArgumentException("الصنف غير محدد."))
                    val amount = proposal.parameters["totalCost"]?.toDoubleOrNull() ?: return Result.failure(IllegalArgumentException("قيمة الشراء غير محددة."))
                    val quantity = proposal.parameters["quantity"]?.toDoubleOrNull() ?: 1.0
                    repository.recordPurchase(
                        Purchase(
                            date = today(),
                            item = item,
                            quantity = quantity,
                            unitPrice = if (quantity > 0) amount / quantity else amount,
                            totalCost = amount,
                            paid = amount,
                            paymentMethod = "نقداً",
                            supplier = proposal.parameters["supplier"] ?: "غير محدد",
                            notes = "تم التسجيل عبر بليغ"
                        )
                    )
                    Result.success("تم تسجيل شراء " + item + " بقيمة " + amount + " جنيه.")
                }
                BaleghActionType.CREATE_SALE -> {
                    val item = proposal.parameters["item"] ?: return Result.failure(IllegalArgumentException("الصنف غير محدد."))
                    val amount = proposal.parameters["totalRevenue"]?.toDoubleOrNull() ?: return Result.failure(IllegalArgumentException("قيمة البيع غير محددة."))
                    val quantity = proposal.parameters["quantity"]?.toDoubleOrNull() ?: 1.0
                    repository.recordSale(
                        Sale(
                            date = today(),
                            item = item,
                            quantity = quantity,
                            unitPrice = if (quantity > 0) amount / quantity else amount,
                            totalRevenue = amount,
                            received = amount,
                            paymentMethod = "نقداً",
                            customer = proposal.parameters["customer"] ?: "غير محدد",
                            notes = "تم التسجيل عبر بليغ"
                        )
                    )
                    Result.success("تم تسجيل بيع " + item + " بقيمة " + amount + " جنيه.")
                }
                BaleghActionType.RECORD_OBSERVATION -> {
                    val details = proposal.parameters["details"] ?: return Result.failure(IllegalArgumentException("الملاحظة غير محددة."))
                    repository.insertMeeting(
                        Meeting(
                            title = "ملاحظة ميدانية",
                            date = today(),
                            location = "المزرعة",
                            attendees = "أحمد",
                            agenda = details,
                            notes = details
                        )
                    )
                    Result.success("تم تسجيل الملاحظة الميدانية.")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
