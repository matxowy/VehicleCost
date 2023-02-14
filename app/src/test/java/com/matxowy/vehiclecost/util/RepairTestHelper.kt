package com.matxowy.vehiclecost.util

import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModelTest

object RepairTestHelper {
    fun repair() = Repair(
        title = TITLE,
        mileage = MILEAGE,
        cost = COST,
        date = DATE,
        time = TIME,
        comments = COMMENTS,
        vehicleId = VEHICLE_ID,
    )

    fun listOfRepairs() = listOf(repair(), repair(), repair())

    const val VEHICLE_ID = 0
    const val TITLE = "Repair"
    const val MILEAGE = 100
    const val DATE = "11.11.2011"
    const val TIME = "12:54"
    const val COST = 190.03
    const val COMMENTS = "Comment"
}