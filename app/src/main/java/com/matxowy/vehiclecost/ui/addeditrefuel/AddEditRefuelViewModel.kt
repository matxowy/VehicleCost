package com.matxowy.vehiclecost.ui.addeditrefuel

import android.widget.AutoCompleteTextView
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.entity.Refuel

class AddEditRefuelViewModel @ViewModelInject constructor(
    private val refuelDao: RefuelDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val refuel = state.get<Refuel>("refuel")

    var mileage = state.get<Int>("refuelMileage") ?: refuel?.mileage ?: ""
        set(value) {
            field = value
            state.set("refuelMileage", value)
        }

    var date = state.get<String>("refuelDate") ?: refuel?.date ?: ""
        set(value) {
            field = value
            state.set("refuelDate", value)
        }

    var time = state.get<String>("refuelTime") ?: refuel?.time ?: ""
        set(value) {
            field = value
            state.set("refuelTime", value)
        }

    var amountOfFuel = state.get<Double>("refuelAmountOfFuel") ?: refuel?.amountOfFuel ?: ""
        set(value) {
            field = value
            state.set("refuelAmountOfFuel", value)
        }

    var cost = state.get<Double>("refuelCost") ?: refuel?.cost ?: ""
        set(value) {
            field = value
            state.set("refuelCost", value)
        }

    var price = state.get<Double>("refuelPrice") ?: refuel?.price ?: ""
        set(value) {
            field = value
            state.set("refuelPrice", value)
        }

    var fullRefueled = state.get<Boolean>("refuelFullRefueled") ?: refuel?.fullRefueled ?: false
        set(value) {
            field = value
            state.set("refuelFullRefueled", value)
        }

    var comments = state.get<String>("refuelComments") ?: refuel?.comments ?: ""
        set(value) {
            field = value
            state.set("refuelComments", value)
        }

    var fuelType = state.get<String>("refuelType") ?: refuel?.fuelType ?: ""
        set(value) {
            field = value
            state.set("refuelType", value)
        }

}