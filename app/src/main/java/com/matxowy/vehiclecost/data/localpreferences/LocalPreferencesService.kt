package com.matxowy.vehiclecost.data.localpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class LocalPreferencesService @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : LocalPreferencesApi {

    companion object {
        const val CURRENT_PICKED_CAR = "current_picked_car"
    }

    override fun saveSelectedVehiclePosition(position: Int) {
        sharedPreferences.edit().putInt(CURRENT_PICKED_CAR, position).apply()
    }

    override fun getSelectedVehiclePosition() =
        sharedPreferences.getInt(CURRENT_PICKED_CAR, 0)

    // Method to get vehicle id, uses + 1 as in database id counting is from 0
    override fun getSelectedVehicleId() =
        sharedPreferences.getInt(CURRENT_PICKED_CAR, 0) + 1

}