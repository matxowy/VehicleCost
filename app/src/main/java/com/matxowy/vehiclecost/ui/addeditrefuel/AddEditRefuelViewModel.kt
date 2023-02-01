package com.matxowy.vehiclecost.ui.addeditrefuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditRefuelViewModel @Inject constructor(
    private val refuelDao: RefuelDao,
    private val vehicleDao: VehicleDao,
    private val state: SavedStateHandle,
    val localPreferences: LocalPreferencesApi,
) : ViewModel() {

    val refuel = state.get<Refuel>(REFUEL_STATE_KEY)

    var mileage = state.get<Int>(MILEAGE_STATE_KEY) ?: refuel?.mileage ?: ""
        set(value) {
            field = value
            state[MILEAGE_STATE_KEY] = value
        }

    var date = state.get<String>(DATE_STATE_KEY) ?: refuel?.date ?: LocalDateConverter.dateToString(LocalDate.now())
        set(value) {
            field = value
            state[DATE_STATE_KEY] = value
        }

    var time = state.get<String>(TIME_STATE_KEY) ?: refuel?.time ?: LocalDateConverter.timeToString(LocalDateTime.now())
        set(value) {
            field = value
            state[TIME_STATE_KEY] = value
        }

    var amountOfFuel = state.get<Double>(AMOUNT_OF_FUEL_STATE_KEY) ?: refuel?.amountOfFuel ?: ""
        set(value) {
            field = value
            state[AMOUNT_OF_FUEL_STATE_KEY] = value
        }

    var cost = state.get<Double>(COST_STATE_KEY) ?: refuel?.cost ?: ""
        set(value) {
            field = value
            state[COST_STATE_KEY] = value
        }

    var price = state.get<Double>(PRICE_STATE_KEY) ?: refuel?.price ?: ""
        set(value) {
            field = value
            state[PRICE_STATE_KEY] = value
        }

    var fullRefueled = state.get<Boolean>(FULL_REFUELED_STATE_KEY) ?: refuel?.fullRefueled ?: false
        set(value) {
            field = value
            state[FULL_REFUELED_STATE_KEY] = value
        }

    var comments = state.get<String>(COMMENTS_STATE_KEY) ?: refuel?.comments ?: ""
        set(value) {
            field = value
            state[COMMENTS_STATE_KEY] = value
        }

    var fuelType = state.get<String>(FUEL_TYPE_STATE_KEY) ?: refuel?.fuelType ?: "ON"
        set(value) {
            field = value
            state[FUEL_TYPE_STATE_KEY] = value
        }

    private val addEditRefuelEventChannel = Channel<AddEditRefuelEvent>()
    val addEditRefuelEvent = addEditRefuelEventChannel.receiveAsFlow()

    private val selectedVehicleId = localPreferences.getSelectedVehicleId()
    var lastMileage = refuelDao.getLastMileage(selectedVehicleId).asLiveData()

    fun onSaveRefueledClick() {
        if (mileage.toString().isBlank()
            || amountOfFuel.toString().isBlank()
            || cost.toString().isBlank()
            || price.toString().isBlank()
        ) {
            showFieldsCannotBeEmptyMessage()
            return
        }

        // The new mileage can't be less than previous
        if (refuel == null) {
            lastMileage.value?.let { lastMileage ->
                if (mileage.toString().toInt() <= lastMileage) {
                    showMileageCannotBeLessThanPreviousMessage()
                    return
                }
            }
        }

        if (refuel != null) {
            val updatedRefuel = refuel.copy(
                mileage = mileage.toString().toInt(),
                date = date.toString(),
                time = time.toString(),
                amountOfFuel = amountOfFuel.toString().toDouble(),
                cost = cost.toString().toDouble(),
                price = price.toString().toDouble(),
                fuelType = fuelType,
                fullRefueled = fullRefueled,
                comments = comments,
                vehicleId = selectedVehicleId,
            )
            updateRefuel(updatedRefuel)
        } else {
            val refuel = Refuel(
                mileage = mileage.toString().toInt(),
                date = date.toString(),
                time = time.toString(),
                amountOfFuel = amountOfFuel.toString().toDouble(),
                cost = cost.toString().toDouble(),
                price = price.toString().toDouble(),
                fuelType = fuelType,
                fullRefueled = fullRefueled,
                comments = comments,
                vehicleId = selectedVehicleId,
            )
            createRefuel(refuel)
        }
    }

    private fun showFieldsCannotBeEmptyMessage() = viewModelScope.launch {
        addEditRefuelEventChannel.send(AddEditRefuelEvent.ShowFieldsCannotBeEmptyMessage)
    }

    private fun showMileageCannotBeLessThanPreviousMessage() = viewModelScope.launch {
        addEditRefuelEventChannel.send(AddEditRefuelEvent.ShowMileageCannotBeLessThanPreviousMessage)
    }

    private fun updateRefuel(updatedRefuel: Refuel) = viewModelScope.launch(Dispatchers.IO) {
        try {
            vehicleDao.updateMileageOfVehicle(updatedRefuel.vehicleId, updatedRefuel.mileage)
            refuelDao.update(updatedRefuel)
            addEditRefuelEventChannel.send(AddEditRefuelEvent.NavigateToHistoryWithResult(EDIT_RESULT_OK))
        } catch (e: Exception) {
            addEditRefuelEventChannel.send(AddEditRefuelEvent.ShowDefaultErrorMessage)
        }
    }

    private fun createRefuel(refuel: Refuel) = viewModelScope.launch(Dispatchers.IO) {
        try {
            vehicleDao.updateMileageOfVehicle(refuel.vehicleId, refuel.mileage)
            refuelDao.insert(refuel)
            addEditRefuelEventChannel.send(AddEditRefuelEvent.NavigateToHistoryWithResult(ADD_RESULT_OK))
        } catch (e: Exception) {
            addEditRefuelEventChannel.send(AddEditRefuelEvent.ShowDefaultErrorMessage)
        }
    }

    companion object {
        const val REFUEL_STATE_KEY = "refuel"
        const val MILEAGE_STATE_KEY = "refuelMileage"
        const val DATE_STATE_KEY = "refuelDate"
        const val TIME_STATE_KEY = "refuelTime"
        const val AMOUNT_OF_FUEL_STATE_KEY = "refuelAmountOfFuel"
        const val COST_STATE_KEY = "refuelCost"
        const val PRICE_STATE_KEY = "refuelPrice"
        const val FULL_REFUELED_STATE_KEY = "refuelFullRefueled"
        const val COMMENTS_STATE_KEY = "refuelComments"
        const val FUEL_TYPE_STATE_KEY = "refuelType"
    }

    sealed class AddEditRefuelEvent {
        object ShowDefaultErrorMessage : AddEditRefuelEvent()
        object ShowFieldsCannotBeEmptyMessage : AddEditRefuelEvent()
        object ShowMileageCannotBeLessThanPreviousMessage : AddEditRefuelEvent()
        data class NavigateToHistoryWithResult(val result: Int) : AddEditRefuelEvent()
    }
}
