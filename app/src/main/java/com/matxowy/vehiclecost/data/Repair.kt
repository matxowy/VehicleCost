package com.matxowy.vehiclecost.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Repair(
    val title: String
) : Parcelable
