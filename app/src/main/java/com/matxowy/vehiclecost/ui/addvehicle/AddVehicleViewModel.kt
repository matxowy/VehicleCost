package com.matxowy.vehiclecost.ui.addvehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val state: SavedStateHandle,
) : ViewModel() {

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

    private val addVehicleChannel = Channel<AddVehicleEvent>()
    val addVehicleEvent = addVehicleChannel.receiveAsFlow()

    fun onAddVehicleButtonClick() {
        if (vehicleName.isBlank() || vehicleMileage.toString().isBlank()) {
            showInvalidInputMessage()
            return
        }

        val newVehicle = Vehicle(
            name = vehicleName,
            mileage = vehicleMileage.toString().toInt(),
        )
        createVehicle(newVehicle)
    }

    private fun createVehicle(newVehicle: Vehicle) = viewModelScope.launch {
        try {
            vehicleDao.insert(newVehicle)
            addVehicleChannel.send(AddVehicleEvent.NavigateToStatisticsWithResult(ADD_RESULT_OK))
        } catch (e: Exception) {
            addVehicleChannel.send(AddVehicleEvent.ShowAddErrorMessage)
        }
    }

    private fun showInvalidInputMessage() = viewModelScope.launch {
        addVehicleChannel.send(AddVehicleEvent.ShowInvalidDataMessage)
    }

    companion object {
        const val NAME_STATE_KEY = "vehicleName"
        const val MILEAGE_STATE_KEY = "vehicleMileage"
    }

    sealed class AddVehicleEvent {
        object ShowInvalidDataMessage : AddVehicleEvent()
        object ShowAddErrorMessage : AddVehicleEvent()
        data class NavigateToStatisticsWithResult(val result: Int) : AddVehicleEvent()
    }
}

