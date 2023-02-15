package com.matxowy.vehiclecost.util

import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import java.text.DecimalFormat

fun Int.addSpace(): String {
    val df = DecimalFormat("#,###,###")
    return df.format(this).replace(",", " ")
}

fun Int.returnValueForField(): String =
    if (this == DEFAULT_INT_VALUE) ""
    else this.toString()

fun Int.hasDefaultValue(): Boolean = this == DEFAULT_INT_VALUE