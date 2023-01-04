package com.matxowy.vehiclecost.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val vehicleId: Int = 0,
    val name: String,
    val mileage: Int,
)
