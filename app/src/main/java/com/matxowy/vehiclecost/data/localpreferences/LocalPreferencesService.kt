package com.matxowy.vehiclecost.data.localpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class LocalPreferencesService @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : LocalPreferencesApi {

    companion object {
        const val CURRENT_PICKED_CAR = "current_picked_car"
    }

    override fun saveSelectedVehicleId(position: Int) {
        sharedPreferences.edit().putInt(CURRENT_PICKED_CAR, position).apply()
    }

    override fun getSelectedVehicleId() =
        sharedPreferences.getInt(CURRENT_PICKED_CAR, 1)
}