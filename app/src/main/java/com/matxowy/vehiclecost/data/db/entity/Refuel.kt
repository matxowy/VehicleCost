package com.matxowy.vehiclecost.data.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

@Entity(tableName = "refuel")
@Parcelize
data class Refuel (
    val mileage: Int,
    val date: String,
    val time: String,
    val amountOfFuel: Double,
    val cost: Double,
    val price: Double,
    val fullRefueled: Boolean = false,
    val comments: String = "",
    @PrimaryKey(autoGenerate = true) val id: Int = 0
    ) : Parcelable