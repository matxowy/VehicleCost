package com.matxowy.vehiclecost.data.localpreferences

interface LocalPreferencesApi {
    fun saveSelectedVehicleId(position: Int)
    fun getSelectedVehicleId(): Int
}