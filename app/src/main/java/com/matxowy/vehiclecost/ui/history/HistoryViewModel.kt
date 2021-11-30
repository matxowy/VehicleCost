package com.matxowy.vehiclecost.ui.history

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao

class HistoryViewModel @ViewModelInject constructor(
    private val refuelDao: RefuelDao,
    private val repairDao: RepairDao
) : ViewModel() {

    val refuels = refuelDao.getRefuels().asLiveData()
    val repairs = repairDao.getRepairs().asLiveData()
}