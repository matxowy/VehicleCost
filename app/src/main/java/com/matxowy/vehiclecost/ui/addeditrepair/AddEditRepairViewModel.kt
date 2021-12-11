package com.matxowy.vehiclecost.ui.addeditrepair

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Repair

class AddEditRepairViewModel @ViewModelInject constructor(
    private val repairDao: RepairDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val repair = state.get<Repair>("repair")

    var title = state.get<String>("repairTitle") ?: repair?.title ?: ""
        set(value) {
            field = value
            state.set("repairTitle", value)
        }

    var mileage = state.get<Int>("repairMileage") ?: repair?.mileage ?: ""
        set(value) {
            field = value
            state.set("repairMileage", value)
        }

    var cost = state.get<Double>("repairCost") ?: repair?.cost ?: ""
        set(value) {
            field = value
            state.set("repairCost", value)
        }

    var date = state.get<String>("repairDate") ?: repair?.date ?: ""
        set(value) {
            field = value
            state.set("repairDate", value)
        }

    var time = state.get<String>("repairTime") ?: repair?.time ?: ""
        set(value) {
            field = value
            state.set("repairTime", value)
        }

    var comments = state.get<String>("repairComments") ?: repair?.comments ?: ""
        set(value) {
            field = value
            state.set("repairComments", value)
        }

}