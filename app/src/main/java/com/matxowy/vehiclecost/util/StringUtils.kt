package com.matxowy.vehiclecost.util

object StringUtils {
    fun trimTrailingZero(value: String?): String? {
        return if (!value.isNullOrEmpty()) {
            if (value.indexOf(".") < 0) {
                value
            } else {
                value.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
            }
        } else {
            value
        }
    }
}