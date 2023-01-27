package com.matxowy.vehiclecost.data.localpreferences

import android.content.Context
import android.content.SharedPreferences
import com.matxowy.vehiclecost.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalPreferencesService @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    @ApplicationContext private val context: Context,
) : LocalPreferencesApi {

    companion object {
        const val CURRENT_PICKED_CAR_POSITION = "current_picked_car_position"
        const val CURRENT_PICKED_CAR_NAME = "current_picked_car_name"
        const val DEFAULT_CAR_POSITION = 1
    }

    override fun saveSelectedVehiclePosition(position: Int) {
        sharedPreferences.edit().putInt(CURRENT_PICKED_CAR_POSITION, position).apply()
    }

    override fun getSelectedVehiclePosition() =
        sharedPreferences.getInt(CURRENT_PICKED_CAR_POSITION, DEFAULT_CAR_POSITION)

    override fun saveSelectedVehicleName(name: String) {
        sharedPreferences.edit().putString(CURRENT_PICKED_CAR_NAME, name).apply()
    }

    override fun getSelectedVehicleName() =
        sharedPreferences.getString(CURRENT_PICKED_CAR_NAME, context.getString(R.string.default_vehicle))!!
}