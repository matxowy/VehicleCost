package com.matxowy.vehiclecost.util

import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_DOUBLE_VALUE
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun Double.toPrettyString() =
    if (this - this.toLong() == 0.0)
        String.format("%d", this.toLong())
    else
        String.format("%s", this)

fun Double.decimalFormat(): String {
    val df = DecimalFormat("0.##")
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}

fun Double.returnValueForField(): String =
    if (this == DEFAULT_DOUBLE_VALUE) ""
    else this.toString().trimTrailingZero()

fun Double.hasDefaultValue(): Boolean = this == DEFAULT_DOUBLE_VALUE
