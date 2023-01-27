package com.matxowy.vehiclecost.data.localpreferences

interface LocalPreferencesApi {
    fun saveSelectedVehiclePosition(position: Int)
    // todo add getting vehicle id and use it instead of position in most cases
    fun getSelectedVehiclePosition(): Int
    fun saveSelectedVehicleName(name: String)
    fun getSelectedVehicleName(): String
}