package com.matxowy.vehiclecost.ui.addeditrepair

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.matxowy.vehiclecost.data.db.dao.RepairDao

class AddEditRepairViewModel @ViewModelInject constructor(
    private val repairDao: RepairDao
) : ViewModel() {
}