package com.matxowy.vehiclecost.ui.managevehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_STRING_VALUE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ManageVehicleViewModel @Inject constructor(
    val vehicleDao: VehicleDao,
    private val state: SavedStateHandle,
    @Named("IO") private val coroutineDispatcher: CoroutineDispatcher,
    private val localPreferences: LocalPreferencesApi
) : ViewModel() {

    private val manageVehicleChannel = Channel<ManageVehicleEvents>()
    val manageVehicleEvent = manageVehicleChannel.receiveAsFlow()

    var vehicleId = DEFAULT_VEHICLE_ID

    var vehicleName = DEFAULT_STRING_VALUE
        get() = state.get<String>(NAME_STATE_KEY) ?: DEFAULT_STRING_VALUE
        set(value) {
            field = value
            state[NAME_STATE_KEY] = value
        }

    var vehicleMileage = DEFAULT_INT_VALUE
        get() = state.get<Int>(MILEAGE_STATE_KEY) ?: DEFAULT_INT_VALUE
        set(value) {
            field = value
            state[MILEAGE_STATE_KEY] = value
        }

    val listOfVehicles = vehicleDao.getVehicles().asLiveData()

    fun onSelectVehicle(id: Int) = viewModelScope.launch(coroutineDispatcher) {
        try {
            val vehicle = vehicleDao.getVehicleById(id)

            vehicleId = vehicle.vehicleId
            vehicleName = vehicle.name
            vehicleMileage = vehicle.mileage

            ManageVehicleEvents.SetFieldsWithData.send()
        } catch (e: Exception) {
            ManageVehicleEvents.ShowDefaultErrorMessage.send()
        }
    }

    fun onDeleteVehicleButtonClick(id: Int) = viewModelScope.launch(coroutineDispatcher) {
        try {
            val vehicle = vehicleDao.getVehicleById(id)
            val currentSelectedVehicleId = localPreferences.getSelectedVehicleId()

            if (currentSelectedVehicleId == vehicle.vehicleId) {
                ManageVehicleEvents.ShowCannotDeleteCurrentSelectedVehicleMessage.send()
            } else {
                vehicleDao.delete(vehicle)
                ManageVehicleEvents.ShowDeleteConfirmMessageWithUndoOption(vehicle).send()
            }
        } catch (e: Exception) {
            ManageVehicleEvents.ShowDefaultErrorMessage.send()
        }
    }

    fun onUndoDeleteVehicleClick(vehicle: Vehicle) = viewModelScope.launch(coroutineDispatcher) {
        try {
            vehicleDao.insert(vehicle)
        } catch (e: Exception) {
            ManageVehicleEvents.ShowDefaultErrorMessage.send()
        }
    }

    fun onEditVehicleButtonClick() = viewModelScope.launch(coroutineDispatcher) {
        val vehicle = Vehicle(vehicleId = vehicleId, name = vehicleName, mileage = vehicleMileage.toString().toInt())
        try {
            vehicleDao.update(vehicle)
            ManageVehicleEvents.ShowUpdateConfirmMessage.send()
        } catch (e: Exception) {
            ManageVehicleEvents.ShowEditingErrorMessage.send()
        }
    }

    companion object {
        const val DEFAULT_VEHICLE_ID = 0
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

    private fun ManageVehicleEvents.send() {
        viewModelScope.launch { manageVehicleChannel.send(this@send) }
    }
}