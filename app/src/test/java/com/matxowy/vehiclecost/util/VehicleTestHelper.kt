package com.matxowy.vehiclecost.util

import com.matxowy.vehiclecost.data.db.entity.Vehicle

object VehicleTestHelper {
    fun vehicle() = Vehicle(name = VEHICLE_NAME, mileage = VEHICLE_MILEAGE)

    fun listOfVehicles() = listOf(vehicle(), vehicle(), vehicle())

    const val VEHICLE_ID = 0
    const val VEHICLE_NAME = "MyCar"
    const val VEHICLE_MILEAGE = 100
}