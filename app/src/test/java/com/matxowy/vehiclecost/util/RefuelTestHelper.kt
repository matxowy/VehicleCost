package com.matxowy.vehiclecost.util

import com.matxowy.vehiclecost.data.db.entity.Refuel

object RefuelTestHelper {
    fun refuel() = Refuel(
        mileage = MILEAGE,
        date = DATE,
        time = TIME,
        amountOfFuel = AMOUNT_OF_FUEL,
        cost = COST,
        price = PRICE,
        fuelType = FUEL_TYPE,
        fullRefueled = FULL_REFUELED,
        comments = COMMENTS,
        vehicleId = VEHICLE_ID,
    )

    fun listOfRefuels() = listOf(refuel(), refuel(), refuel())

    const val VEHICLE_ID = 0
    const val MILEAGE = 100
    const val DATE = "11.11.2011"
    const val TIME = "12:54"
    const val AMOUNT_OF_FUEL = 42.23
    const val COST = 190.03
    const val PRICE = 4.5
    const val FULL_REFUELED = true
    const val COMMENTS = "Comment"
    const val FUEL_TYPE = "ON"
}