package com.ahmedpasha.smartfarm.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedpasha.smartfarm.FarmApplication
import com.ahmedpasha.smartfarm.data.models.Attendance
import com.ahmedpasha.smartfarm.data.models.Debt
import com.ahmedpasha.smartfarm.data.models.TreasuryTransaction
import kotlinx.coroutines.launch

fun FarmViewModel.deleteAttendance(item: Attendance) {
    val dao = getApplication<FarmApplication>().database.farmDao()
    viewModelScope.launch { dao.deleteAttendance(item) }
}

fun FarmViewModel.deleteTreasuryTransaction(item: TreasuryTransaction) {
    val dao = getApplication<FarmApplication>().database.farmDao()
    viewModelScope.launch { dao.deleteTreasuryTransaction(item) }
}

fun FarmViewModel.deleteDebt(item: Debt) {
    val dao = getApplication<FarmApplication>().database.farmDao()
    viewModelScope.launch { dao.deleteDebt(item) }
}
