package com.matxowy.vehiclecost.ui.addeditrefuel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.matxowy.vehiclecost.data.db.dao.RefuelDao

class AddEditRefuelViewModel @ViewModelInject constructor(
    private val refuelDao: RefuelDao
) : ViewModel() {

}