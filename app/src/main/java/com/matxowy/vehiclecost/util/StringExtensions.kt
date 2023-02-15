package com.matxowy.vehiclecost.util

import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_DOUBLE_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE

fun String.toIntOrDefaultValue(): Int = if (this.isBlank()) DEFAULT_INT_VALUE else this.toInt()

fun String.toDoubleOrDefaultValue(): Double = if (this.isBlank()) DEFAULT_DOUBLE_VALUE else this.toDouble()

fun String.trimTrailingZero(): String =
    if (this.indexOf(".") < 0) {
        this
    } else {
        this.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
    }