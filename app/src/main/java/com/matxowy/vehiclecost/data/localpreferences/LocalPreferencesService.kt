package com.matxowy.vehiclecost.data.localpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class LocalPreferencesService @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : LocalPreferencesApi {

    companion object {
        const val CURRENT_PICKED_CAR_POSITION = "current_picked_car_position"
        const val CURRENT_PICKED_CAR_ID = "current_picked_car_id"
        const val DEFAULT_CAR_POSITION = 1
        const val DEFAULT_CAR_ID = 1
    }

    override fun saveSelectedVehiclePosition(position: Int) {
        sharedPreferences.edit().putInt(CURRENT_PICKED_CAR_POSITION, position).apply()
    }

    override fun getSelectedVehiclePosition() =
        sharedPreferences.getInt(CURRENT_PICKED_CAR_POSITION, DEFAULT_CAR_POSITION)

    override fun saveSelectedVehicleId(id: Int) {
        sharedPreferences.edit().putInt(CURRENT_PICKED_CAR_ID, id).apply()
    }

    override fun getSelectedVehicleId(): Int =
        sharedPreferences.getInt(CURRENT_PICKED_CAR_ID, DEFAULT_CAR_ID)
}