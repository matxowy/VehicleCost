package com.matxowy.vehiclecost.ui.addeditrefuel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditRefuelViewModel @Inject constructor(
    private val refuelDao: RefuelDao,
    private val state: SavedStateHandle,
    @ApplicationContext private val context: Context,
    val localPreferences: LocalPreferencesApi,
) : ViewModel() {

    val refuel = state.get<Refuel>("refuel")

    var mileage = state.get<Int>("refuelMileage") ?: refuel?.mileage ?: ""
        set(value) {
            field = value
            state["refuelMileage"] = value
        }

    var date = state.get<String>("refuelDate") ?: refuel?.date ?: LocalDateConverter.dateToString(LocalDate.now())
        set(value) {
            field = value
            state["refuelDate"] = value
        }

    var time = state.get<String>("refuelTime") ?: refuel?.time ?: LocalDateConverter.timeToString(LocalDateTime.now())
        set(value) {
            field = value
            state["refuelTime"] = value
        }

    var amountOfFuel = state.get<Double>("refuelAmountOfFuel") ?: refuel?.amountOfFuel ?: ""
        set(value) {
            field = value
            state["refuelAmountOfFuel"] = value
        }

    var cost = state.get<Double>("refuelCost") ?: refuel?.cost ?: ""
        set(value) {
            field = value
            state["refuelCost"] = value
        }

    var price = state.get<Double>("refuelPrice") ?: refuel?.price ?: ""
        set(value) {
            field = value
            state["refuelPrice"] = value
        }

    var fullRefueled = state.get<Boolean>("refuelFullRefueled") ?: refuel?.fullRefueled ?: false
        set(value) {
            field = value
            state["refuelFullRefueled"] = value
        }

    var comments = state.get<String>("refuelComments") ?: refuel?.comments ?: ""
        set(value) {
            field = value
            state["refuelComments"] = value
        }

    var fuelType = state.get<String>("refuelType") ?: refuel?.fuelType ?: "ON"
        set(value) {
            field = value
            state["refuelType"] = value
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
            showInvalidInputMessage(context.getString(R.string.required_fields_cannot_be_empty_text))
            return
        }

        // The new mileage can't be less than previous
        if (refuel == null) {
            lastMileage.value?.let { lastMileage ->
                if (mileage.toString().toInt() <= lastMileage) {
                    showInvalidInputMessage(context.getString(R.string.mileage_cannot_be_less_than_previous))
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

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditRefuelEventChannel.send(AddEditRefuelEvent.ShowInvalidInputMessage(text))
    }

    private fun updateRefuel(updatedRefuel: Refuel) = viewModelScope.launch {
        refuelDao.update(updatedRefuel)
        addEditRefuelEventChannel.send(AddEditRefuelEvent.NavigateToHistoryWithResult(EDIT_RESULT_OK))
    }

    private fun createRefuel(refuel: Refuel) = viewModelScope.launch {
        refuelDao.insert(refuel)
        addEditRefuelEventChannel.send(AddEditRefuelEvent.NavigateToHistoryWithResult(ADD_RESULT_OK))
    }

    sealed class AddEditRefuelEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditRefuelEvent()
        data class NavigateToHistoryWithResult(val result: Int) : AddEditRefuelEvent()
    }
}
