package com.matxowy.vehiclecost.util

import java.text.DecimalFormat

fun Int.addSpace(): String {
    val df = DecimalFormat("#,###,###")
    return df.format(this).replace(",", " ")
}