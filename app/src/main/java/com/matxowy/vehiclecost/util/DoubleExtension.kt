package com.matxowy.vehiclecost.util

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun Double.toPrettyString() =
    if(this - this.toLong() == 0.0)
        String.format("%d", this.toLong())
    else
        String.format("%s", this)

fun Double.decimalFormat(): String {
    val df = DecimalFormat("0.##")
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}