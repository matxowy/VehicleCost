package com.matxowy.vehiclecost.data.localpreferences

interface LocalPreferencesApi {
    fun saveSelectedVehiclePosition(position: Int)
    fun getSelectedVehiclePosition(): Int
    fun getSelectedVehicleId(): Int
}