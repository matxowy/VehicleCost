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

    val listOfVehicles = vehicleDao.getVehicles().asLiveData()

    fun onSelectVehicle(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val vehicle = vehicleDao.getVehicleById(id)

            vehicleId = vehicle.vehicleId
            vehicleName = vehicle.name
            vehicleMileage = vehicle.mileage

            manageVehicleChannel.send(ManageVehicleEvents.SetFieldsWithData)
        } catch (e: Exception) {
            manageVehicleChannel.send(ManageVehicleEvents.ShowDefaultErrorMessage)
        }
    }

    fun onDeleteVehicleButtonClick(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val vehicle = vehicleDao.getVehicleById(id)
            val currentSelectedVehicleId = localPreferences.getSelectedVehicleId()

            if (currentSelectedVehicleId == vehicle.vehicleId) {
                manageVehicleChannel.send(ManageVehicleEvents.ShowCannotDeleteCurrentSelectedVehicleMessage)
            } else {
                vehicleDao.delete(vehicle)
                manageVehicleChannel.send(ManageVehicleEvents.ShowDeleteConfirmMessageWithUndoOption(vehicle))
            }
        } catch (e: Exception) {
            manageVehicleChannel.send(ManageVehicleEvents.ShowDefaultErrorMessage)
        }
    }

    fun onUndoDeleteVehicleClick(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) {
        try {
            vehicleDao.insert(vehicle)
        } catch (e: Exception) {
            manageVehicleChannel.send(ManageVehicleEvents.ShowDefaultErrorMessage)
        }
    }

    fun onEditVehicleButtonClick() = viewModelScope.launch(Dispatchers.IO) {
        val vehicle = Vehicle(vehicleId = vehicleId, name = vehicleName, mileage = vehicleMileage.toString().toInt())
        try {
            vehicleDao.update(vehicle)
            manageVehicleChannel.send(ManageVehicleEvents.ShowUpdateConfirmMessage)
        } catch (e: Exception) {
            manageVehicleChannel.send(ManageVehicleEvents.ShowEditingErrorMessage)
        }
    }

    companion object {
        const val NAME_STATE_KEY = "vehicleName"
        const val MILEAGE_STATE_KEY = "vehicleMileage"
    }

    sealed class ManageVehicleEvents {
        object SetFieldsWithData : ManageVehicleEvents()
        object ShowDefaultErrorMessage : ManageVehicleEvents()
        object ShowEditingErrorMessage : ManageVehicleEvents()
        object ShowUpdateConfirmMessage : ManageVehicleEvents()
        object ShowCannotDeleteCurrentSelectedVehicleMessage : ManageVehicleEvents()
        data class ShowDeleteConfirmMessageWithUndoOption(val vehicle: Vehicle) : ManageVehicleEvents()
    }
}