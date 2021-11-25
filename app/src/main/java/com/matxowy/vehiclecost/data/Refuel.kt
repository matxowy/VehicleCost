package com.matxowy.vehiclecost.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Refuel (
    val mileage: Int
    ) : Parcelable