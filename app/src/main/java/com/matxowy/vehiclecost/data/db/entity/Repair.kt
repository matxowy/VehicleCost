package com.matxowy.vehiclecost.data.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "repair")
@Parcelize
data class Repair(
    val title: String,
    val mileage: Int,
    val cost: Double,
    val date: String,
    val time: String,
    val comments: String = "",
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
) : Parcelable
