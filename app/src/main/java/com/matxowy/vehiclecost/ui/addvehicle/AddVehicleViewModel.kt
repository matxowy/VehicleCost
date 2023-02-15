package com.matxowy.vehiclecost.ui.addvehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_STRING_VALUE
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.hasDefaultValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val state: SavedStateHandle,
    @Named("IO") private val coroutineDispatcher: CoroutineDispatcher,
) : ViewModel() {

    var vehicleName = state.get<String>(NAME_STATE_KEY) ?: DEFAULT_STRING_VALUE
        set(value) {
            field = value
            state[NAME_STATE_KEY] = value
        }

    var vehicleMileage = state.get<Int>(MILEAGE_STATE_KEY) ?: DEFAULT_INT_VALUE
        set(value) {
            field = value
            state[MILEAGE_STATE_KEY] = value
        }

    private val addVehicleChannel = Channel<AddVehicleEvent>()
    val addVehicleEvent = addVehicleChannel.receiveAsFlow()

    fun onAddVehicleButtonClick() {
        if (vehicleName.isBlank() || vehicleMileage.hasDefaultValue()) {
            showInvalidInputMessage()
            return
        }

        val newVehicle = Vehicle(
            name = vehicleName,
            mileage = vehicleMileage,
        )
        createVehicle(newVehicle)
    }

    private fun createVehicle(newVehicle: Vehicle) = viewModelScope.launch(coroutineDispatcher) {
        try {
            vehicleDao.insert(newVehicle)
            AddVehicleEvent.NavigateToStatisticsWithResult(ADD_RESULT_OK).send()
        } catch (e: Exception) {
            AddVehicleEvent.ShowAddErrorMessage.send()
        }
    }

    private fun showInvalidInputMessage() = AddVehicleEvent.ShowInvalidDataMessage.send()

    sealed class AddVehicleEvent {
        object ShowInvalidDataMessage : AddVehicleEvent()
        object ShowAddErrorMessage : AddVehicleEvent()
        data class NavigateToStatisticsWithResult(val result: Int) : AddVehicleEvent()
    }

    private fun AddVehicleEvent.send() {
        viewModelScope.launch { addVehicleChannel.send(this@send) }
    }

    companion object {
        const val NAME_STATE_KEY = "vehicleName"
        const val MILEAGE_STATE_KEY = "vehicleMileage"
    }
}

