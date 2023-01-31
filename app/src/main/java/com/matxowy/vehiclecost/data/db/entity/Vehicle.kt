package com.matxowy.vehiclecost.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle", indices = [Index(value = ["name"], unique = true)])
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val vehicleId: Int = 0,
    val name: String,
    val mileage: Int = 0,
) {
    override fun toString(): String = name
}
