package com.matxowy.vehiclecost.ui.managevehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageVehicleViewModel @Inject constructor(
    val vehicleDao: VehicleDao,
    private val state: SavedStateHandle,
    private val localPreferences: LocalPreferencesApi
) : ViewModel() {

    private val manageVehicleChannel = Channel<ManageVehicleEvents>()
    val manageVehicleEvent = manageVehicleChannel.receiveAsFlow()

    var vehicleId = 0

    var vehicleName = state.get<String>(NAME_STATE_KEY) ?: ""
        set(value) {
            field = value
            state[NAME_STATE_KEY] = value
        }

    var vehicleMileage = state.get<Int>(MILEAGE_STATE_KEY) ?: ""
        set(value) {
            field = value
            state[MILEAGE_STATE_KEY] = value
        }

    val vehiclesNames = vehicleDao.getVehiclesNames().asLiveData()

    fun onSelectVehicle(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val vehicle = vehicleDao.getVehicleByName(name)

        vehicleId = vehicle.vehicleId
        vehicleName = vehicle.name
        vehicleMileage = vehicle.mileage

        manageVehicleChannel.send(ManageVehicleEvents.SetFieldsWithData)
    }

    fun onDeleteVehicleButtonClick(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val vehicle = vehicleDao.getVehicleByName(name)
        val currentSelectedVehicleName = localPreferences.getSelectedVehicleName()

        if (currentSelectedVehicleName == vehicle.name){
            manageVehicleChannel.send(ManageVehicleEvents.ShowCannotDeleteCurrentSelectedVehicleMessage)
        } else {
            vehicleDao.delete(vehicle)
            manageVehicleChannel.send(ManageVehicleEvents.ShowDeleteConfirmMessageWithUndoOption(vehicle))
        }
    }

    fun onUndoDeleteVehicleClick(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) {
        vehicleDao.insert(vehicle)
    }

    fun onEditVehicleButtonClick(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentSelectedVehicleName = localPreferences.getSelectedVehicleName()
        val vehicle = Vehicle(vehicleId = vehicleId, name = vehicleName, mileage = vehicleMileage.toString().toInt())
        vehicleDao.update(vehicle)
        if (currentSelectedVehicleName == name) {
            localPreferences.saveSelectedVehicleName(vehicle.name)
        }
        manageVehicleChannel.send(ManageVehicleEvents.ShowUpdateConfirmMessage)
    }

    companion object {
        const val NAME_STATE_KEY = "vehicleName"
        const val MILEAGE_STATE_KEY = "vehicleMileage"
    }

    sealed class ManageVehicleEvents {
        object SetFieldsWithData : ManageVehicleEvents()
        object ShowUpdateConfirmMessage : ManageVehicleEvents()
        object ShowCannotDeleteCurrentSelectedVehicleMessage : ManageVehicleEvents()
        data class ShowDeleteConfirmMessageWithUndoOption(val vehicle: Vehicle) : ManageVehicleEvents()
    }
}