package com.matxowy.vehiclecost.ui.addeditvehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditVehicleViewModel @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val state: SavedStateHandle,
) : ViewModel() {

    val vehicle = state.get<Vehicle>(VEHICLE_STATE_KEY)

    var vehicleName = state.get<String>(NAME_STATE_KEY) ?: vehicle?.name ?: ""
        set(value) {
            field = value
            state[NAME_STATE_KEY] = value
        }

    var vehicleMileage = state.get<Int>(MILEAGE_STATE_KEY) ?: vehicle?.mileage ?: ""
        set(value) {
            field = value
            state[MILEAGE_STATE_KEY] = value
        }

    private val addEditVehicleChannel = Channel<AddEditVehicleEvent>()
    val addEditVehicleEvent = addEditVehicleChannel.receiveAsFlow()

    fun onAddVehicleButtonClick() {
        if (vehicleName.isBlank() || vehicleMileage.toString().isBlank()) {
            showInvalidInputMessage()
            return
        }

        if (vehicle != null) {
            val updatedVehicle = vehicle.copy(
                name = vehicleName,
                mileage = vehicleMileage.toString().toInt(),
            )
            updateVehicle(updatedVehicle)
        } else {
            val newVehicle = Vehicle(
                name = vehicleName,
                mileage = vehicleMileage.toString().toInt(),
            )
            createVehicle(newVehicle)
        }

    }

    private fun createVehicle(newVehicle: Vehicle) = viewModelScope.launch {
        // TODO Add try catch block for all types of similar actions with new bad result code
        vehicleDao.insert(newVehicle)
        addEditVehicleChannel.send(AddEditVehicleEvent.NavigateToStatisticsWithResult(ADD_RESULT_OK))
    }

    private fun updateVehicle(updatedVehicle: Vehicle) = viewModelScope.launch {
        vehicleDao.update(updatedVehicle)
        addEditVehicleChannel.send(AddEditVehicleEvent.NavigateToStatisticsWithResult(EDIT_RESULT_OK))
    }

    private fun showInvalidInputMessage() = viewModelScope.launch {
        addEditVehicleChannel.send(AddEditVehicleEvent.ShowInvalidDataMessage)
    }

    companion object {
        const val VEHICLE_STATE_KEY = "vehicle"
        const val NAME_STATE_KEY = "vehicleName"
        const val MILEAGE_STATE_KEY = "vehicleMileage"
    }

    sealed class AddEditVehicleEvent {
        object ShowInvalidDataMessage : AddEditVehicleEvent()
        data class NavigateToStatisticsWithResult(val result: Int) : AddEditVehicleEvent()
    }
}

