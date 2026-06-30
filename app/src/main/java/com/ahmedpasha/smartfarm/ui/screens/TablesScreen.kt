package com.ahmedpasha.smartfarm.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahmedpasha.smartfarm.data.models.*
import com.ahmedpasha.smartfarm.ui.components.CustomChip
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import com.ahmedpasha.smartfarm.util.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(viewModel: FarmViewModel) {
    val allTabs = listOf(
        "أراضي", "محاصيل", "عمليات", "مخازن", "حركات مخزن",
        "حيوانات", "إنتاج حيواني", "عمال", "حضور",
        "جهات اتصال", "معدات", "صيانة", "ري",
        "مشتريات", "مبيعات", "خزينة", "ديون"
    )

    val selectedTab by viewModel.selectedTab.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allTabs.forEach { tab ->
                CustomChip(text = tab, selected = selectedTab == tab, onClick = { viewModel.setSelectedTab(tab) }, modifier = Modifier.height(36.dp))
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 8.dp))

        when (selectedTab) {
            "أراضي" -> EntityListWithAdd(
                items = viewModel.lands.collectAsState().value,
                fields = listOf("الكود", "الاسم", "المساحة", "الموقع", "نوع التربة", "الحالة"),
                extractData = { it -> listOf(it.code, it.name, "${it.area} فدان", it.location, it.soilType, it.status) },
                onDelete = { viewModel.deleteLand(it as Land) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddLandDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertLand(it); showAddDialog = false }) }
            )
            "محاصيل" -> EntityListWithAdd(
                items = viewModel.crops.collectAsState().value,
                fields = listOf("الكود", "المحصول", "الموسم", "المساحة", "الإنتاج المتوقع", "الحالة"),
                extractData = { it -> listOf(it.code, it.crop, it.season, "${it.area} فدان", "${it.expectedProduction} طن", it.status) },
                onDelete = { viewModel.deleteCrop(it as Crop) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddCropDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertCrop(it); showAddDialog = false }) }
            )
            "عمال" -> EntityListWithAdd(
                items = viewModel.workers.collectAsState().value,
                fields = listOf("الكود", "الاسم", "الوظيفة", "الأجر اليومي", "الحالة", "الهاتف"),
                extractData = { it -> listOf(it.code, it.name, it.job, "${it.dailyRate} ج", it.status, it.phone) },
                onDelete = { viewModel.deleteWorker(it as Worker) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddWorkerDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertWorker(it); showAddDialog = false }) }
            )
            "حضور" -> EntityListWithAdd(
                items = viewModel.attendance.collectAsState().value,
                fields = listOf("التاريخ", "العامل", "الحالة", "تأخير", "مكافأة", "خصم"),
                extractData = { it -> listOf(it.date, it.workerCode, it.status, "${it.delayHours} س", "${it.bonus} ج", "${it.deduction} ج") },
                onDelete = { viewModel.deleteAttendance(it as Attendance) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddAttendanceDialog(viewModel, onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertAttendance(it); showAddDialog = false }) }
            )
            "مشتريات" -> EntityListWithAdd(
                items = viewModel.purchases.collectAsState().value,
                fields = listOf("التاريخ", "الصنف", "الكمية", "الإجمالي", "المدفوع", "المورد"),
                extractData = { it -> listOf(it.date, it.item, "${it.quantity}", "${it.totalCost} ج", "${it.paid} ج", it.supplier) },
                onDelete = { viewModel.deletePurchase(it as Purchase) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddPurchaseDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertPurchase(it); showAddDialog = false }) }
            )
            "مبيعات" -> EntityListWithAdd(
                items = viewModel.sales.collectAsState().value,
                fields = listOf("التاريخ", "الصنف", "الكمية", "الإجمالي", "المستلم", "العميل"),
                extractData = { it -> listOf(it.date, it.item, "${it.quantity}", "${it.totalRevenue} ج", "${it.received} ج", it.customer) },
                onDelete = { viewModel.deleteSale(it as Sale) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddSaleDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertSale(it); showAddDialog = false }) }
            )
            "خزينة" -> EntityListWithAdd(
                items = viewModel.treasury.collectAsState().value,
                fields = listOf("التاريخ", "النوع", "المبلغ", "الفئة", "الوصف"),
                extractData = { it -> listOf(it.date, it.transactionType, "${it.amount} ج", it.category, it.description) },
                onDelete = { viewModel.deleteTreasuryTransaction(it as TreasuryTransaction) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddTreasuryDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertTreasuryTransaction(it); showAddDialog = false }) }
            )
            "ديون" -> EntityListWithAdd(
                items = viewModel.debts.collectAsState().value,
                fields = listOf("التاريخ", "الطرف", "النوع", "المبلغ", "تاريخ الاستحقاق", "الحالة"),
                extractData = { it -> listOf(it.date, it.contactName, it.debtType, "${it.amount} ج", it.dueDate, it.status) },
                onDelete = { viewModel.deleteDebt(it as Debt) },
                showAddDialog = showAddDialog,
                onAddClick = { showAddDialog = true },
                addDialog = { AddDebtDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.insertDebt(it); showAddDialog = false }) }
            )
            else -> Text("اختر جدولاً للعرض", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun <T> EntityListWithAdd(
    items: List<T>,
    fields: List<String>,
    extractData: (T) -> List<String>,
    onDelete: (T) -> Unit,
    showAddDialog: Boolean,
    onAddClick: () -> Unit,
    addDialog: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("لا توجد بيانات", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items.size) { index ->
                    val item = items[index]
                    val data = extractData(item)
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                data.forEachIndexed { i, value ->
                                    Text("${fields.getOrNull(i) ?: ""}: $value", style = if (i == 0) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                            }
                            IconButton(onClick = { onDelete(item) }) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = onAddClick, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, contentDescription = "إضافة")
        }
    }
    if (showAddDialog) { addDialog() }
}

// ============ DIALOGS ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLandDialog(onDismiss: () -> Unit, onConfirm: (Land) -> Unit) {
    var code by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }; var location by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }; var codeError by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة أرض") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = code, onValueChange = { code = it; codeError = false }, label = { Text("الكود") }, isError = codeError, supportingText = { if (codeError) Text("مطلوب", color = Color.Red) })
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
            OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("المساحة (فدان)") })
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("الموقع") })
            OutlinedTextField(value = soilType, onValueChange = { soilType = it }, label = { Text("نوع التربة") })
        }},
        confirmButton = { Button(onClick = { if (code.isBlank()) { codeError = true } else onConfirm(Land(code = code, name = name, area = area.toDoubleOrNull() ?: 0.0, location = location, soilType = soilType)) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropDialog(onDismiss: () -> Unit, onConfirm: (Crop) -> Unit) {
    var code by remember { mutableStateOf("") }; var crop by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("شتوي") }; var area by remember { mutableStateOf("") }
    var expectedProd by remember { mutableStateOf("") }; var codeError by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة محصول") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = code, onValueChange = { code = it; codeError = false }, label = { Text("الكود") }, isError = codeError)
            OutlinedTextField(value = crop, onValueChange = { crop = it }, label = { Text("اسم المحصول") })
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CustomChip("شتوي", selected = season == "شتوي", onClick = { season = "شتوي" })
                CustomChip("صيفي", selected = season == "صيفي", onClick = { season = "صيفي" })
            }
            OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("المساحة (فدان)") })
            OutlinedTextField(value = expectedProd, onValueChange = { expectedProd = it }, label = { Text("الإنتاج المتوقع (طن)") })
        }},
        confirmButton = { Button(onClick = { if (code.isBlank()) { codeError = true } else onConfirm(Crop(code = code, landCode = "", crop = crop, season = season, area = area.toDoubleOrNull() ?: 0.0, expectedProduction = expectedProd.toDoubleOrNull() ?: 0.0, plantingDate = DateHelper.getCurrentDate())) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerDialog(onDismiss: () -> Unit, onConfirm: (Worker) -> Unit) {
    var code by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("عامل") }; var dailyRate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }; var codeError by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة عامل") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = code, onValueChange = { code = it; codeError = false }, label = { Text("الكود") }, isError = codeError)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
            OutlinedTextField(value = job, onValueChange = { job = it }, label = { Text("الوظيفة") })
            OutlinedTextField(value = dailyRate, onValueChange = { dailyRate = it }, label = { Text("الأجر اليومي") })
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("الهاتف") })
        }},
        confirmButton = { Button(onClick = { if (code.isBlank()) { codeError = true } else onConfirm(Worker(code = code, name = name, job = job, dailyRate = dailyRate.toDoubleOrNull() ?: 0.0, joinDate = DateHelper.getCurrentDate(), phone = phone)) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttendanceDialog(viewModel: FarmViewModel, onDismiss: () -> Unit, onConfirm: (Attendance) -> Unit) {
    val workers = viewModel.workers.collectAsState().value
    var selectedWorker by remember { mutableStateOf(workers.firstOrNull()) }
    var status by remember { mutableStateOf("حاضر") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("تسجيل حضور") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("العامل: ${selectedWorker?.name ?: "غير محدد"}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomChip("حاضر", selected = status == "حاضر", onClick = { status = "حاضر" }, selectedColor = Color(0xFF10B981))
                CustomChip("غائب", selected = status == "غائب", onClick = { status = "غائب" }, selectedColor = Color(0xFFEF4444))
                CustomChip("إجازة", selected = status == "إجازة", onClick = { status = "إجازة" }, selectedColor = Color(0xFF3B82F6))
            }
        }},
        confirmButton = { Button(onClick = { selectedWorker?.let { onConfirm(Attendance(workerCode = it.code, date = DateHelper.getCurrentDate(), status = status)) } }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseDialog(onDismiss: () -> Unit, onConfirm: (Purchase) -> Unit) {
    var item by remember { mutableStateOf("") }; var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }; var paid by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("نقدًا") }; var supplier by remember { mutableStateOf("") }
    var itemError by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("تسجيل مشتريات") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = item, onValueChange = { item = it; itemError = false }, label = { Text("الصنف") }, isError = itemError, supportingText = { if (itemError) Text("مطلوب", color = Color.Red) })
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") })
            OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("سعر الوحدة") })
            OutlinedTextField(value = paid, onValueChange = { paid = it }, label = { Text("المدفوع") })
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("نقدًا", "شيك", "آجل").forEach { CustomChip(text = it, selected = paymentMethod == it, onClick = { paymentMethod = it }) }
            }
            OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("المورد") })
        }},
        confirmButton = { Button(onClick = { if (item.isBlank()) { itemError = true } else { val qty = quantity.toDoubleOrNull() ?: 0.0; val price = unitPrice.toDoubleOrNull() ?: 0.0; onConfirm(Purchase(date = DateHelper.getCurrentDate(), item = item, quantity = qty, unitPrice = price, totalCost = qty * price, paid = paid.toDoubleOrNull() ?: 0.0, paymentMethod = paymentMethod, supplier = supplier)) } }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(onDismiss: () -> Unit, onConfirm: (Sale) -> Unit) {
    var item by remember { mutableStateOf("") }; var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }; var received by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("نقدًا") }; var customer by remember { mutableStateOf("") }
    var itemError by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("تسجيل مبيعات") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = item, onValueChange = { item = it; itemError = false }, label = { Text("الصنف") }, isError = itemError)
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") })
            OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("سعر الوحدة") })
            OutlinedTextField(value = received, onValueChange = { received = it }, label = { Text("المستلم") })
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("نقدًا", "شيك", "آجل").forEach { CustomChip(text = it, selected = paymentMethod == it, onClick = { paymentMethod = it }) }
            }
            OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("العميل") })
        }},
        confirmButton = { Button(onClick = { if (item.isBlank()) { itemError = true } else { val qty = quantity.toDoubleOrNull() ?: 0.0; val price = unitPrice.toDoubleOrNull() ?: 0.0; onConfirm(Sale(date = DateHelper.getCurrentDate(), item = item, quantity = qty, unitPrice = price, totalRevenue = qty * price, received = received.toDoubleOrNull() ?: 0.0, paymentMethod = paymentMethod, customer = customer)) } }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTreasuryDialog(onDismiss: () -> Unit, onConfirm: (TreasuryTransaction) -> Unit) {
    var transactionType by remember { mutableStateOf("إيداع") }; var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("مبيعات") }; var description by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("حركة خزينة") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CustomChip("إيداع", selected = transactionType == "إيداع", onClick = { transactionType = "إيداع" }, selectedColor = Color(0xFF10B981))
                CustomChip("صرف", selected = transactionType == "صرف", onClick = { transactionType = "صرف" }, selectedColor = Color(0xFFEF4444))
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") })
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("الفئة") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("الوصف") })
        }},
        confirmButton = { Button(onClick = { onConfirm(TreasuryTransaction(date = DateHelper.getCurrentDate(), transactionType = transactionType, amount = amount.toDoubleOrNull() ?: 0.0, category = category, description = description)) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(onDismiss: () -> Unit, onConfirm: (Debt) -> Unit) {
    var contactName by remember { mutableStateOf("") }; var debtType by remember { mutableStateOf("لنا") }
    var amount by remember { mutableStateOf("") }; var dueDate by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة دين") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("الطرف") })
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CustomChip("لنا (ذمم مدينة)", selected = debtType == "لنا", onClick = { debtType = "لنا" })
                CustomChip("علينا (ذمم دائنة)", selected = debtType == "علينا", onClick = { debtType = "علينا" })
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") })
            OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("تاريخ الاستحقاق (YYYY-MM-DD)") })
        }},
        confirmButton = { Button(onClick = { onConfirm(Debt(date = DateHelper.getCurrentDate(), contactName = contactName, debtType = debtType, amount = amount.toDoubleOrNull() ?: 0.0, dueDate = dueDate, status = "مستحق")) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}