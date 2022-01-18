package com.matxowy.vehiclecost.ui.addeditrefuel

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.ui.ADD_REFUEL_RESULT_OK
import com.matxowy.vehiclecost.ui.EDIT_REFUEL_RESULT_OK
import com.matxowy.vehiclecost.util.LocalDateConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class AddEditRefuelViewModel @ViewModelInject constructor(
    private val refuelDao: RefuelDao,
    @Assisted private val state: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val refuel = state.get<Refuel>("refuel")

    var mileage = state.get<Int>("refuelMileage") ?: refuel?.mileage ?: ""
        set(value) {
            field = value
            state.set("refuelMileage", value)
        }

    var date = state.get<String>("refuelDate") ?: refuel?.date ?: LocalDateConverter.dateToString(
        LocalDate.now()
    )
        set(value) {
            field = value
            state.set("refuelDate", value)
        }

    var time = state.get<String>("refuelTime") ?: refuel?.time ?: LocalDateConverter.timeToString(
        LocalDateTime.now()
    )
        set(value) {
            field = value
            state.set("refuelTime", value)
        }

    var amountOfFuel = state.get<Double>("refuelAmountOfFuel") ?: refuel?.amountOfFuel ?: ""
        set(value) {
            field = value
            state.set("refuelAmountOfFuel", value)
        }

    var cost = state.get<Double>("refuelCost") ?: refuel?.cost ?: ""
        set(value) {
            field = value
            state.set("refuelCost", value)
        }

    var price = state.get<Double>("refuelPrice") ?: refuel?.price ?: ""
        set(value) {
            field = value
            state.set("refuelPrice", value)
        }

    var fullRefueled = state.get<Boolean>("refuelFullRefueled") ?: refuel?.fullRefueled ?: false
        set(value) {
            field = value
            state.set("refuelFullRefueled", value)
        }

    var comments = state.get<String>("refuelComments") ?: refuel?.comments ?: ""
        set(value) {
            field = value
            state.set("refuelComments", value)
        }

    var fuelType = state.get<String>("refuelType") ?: refuel?.fuelType ?: "ON"
        set(value) {
            field = value
            state.set("refuelType", value)
        }

    private val addEditRefuelEventChannel = Channel<AddEditRefuelEvent>()
    val addEditRefuelEvent = addEditRefuelEventChannel.receiveAsFlow()

    var lastMileage = refuelDao.getLastMileage().asLiveData()


    fun onSaveRefueledClick() {
        if (mileage.toString().isBlank()
            || amountOfFuel.toString().isBlank()
            || cost.toString().isBlank()
            || price.toString().isBlank()) {
            showInvalidInputMessage(context.getString(R.string.required_fields_cannot_be_empty_text))
            return
        }

        //jeżeli przebieg jest mniejszy od poprzedniego, a data jest późniejsza to zwracać komunikat

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
                comments = comments
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
                comments = comments
            )
            createRefuel(refuel)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditRefuelEventChannel.send(AddEditRefuelEvent.ShowInvalidInputMessage(text))
    }

    private fun updateRefuel(updatedRefuel: Refuel) = viewModelScope.launch {
        refuelDao.update(updatedRefuel)
        addEditRefuelEventChannel.send(AddEditRefuelEvent.NavigateToHistoryWithResult(
            EDIT_REFUEL_RESULT_OK))
    }

    private fun createRefuel(refuel: Refuel) = viewModelScope.launch {
        refuelDao.insert(refuel)
        addEditRefuelEventChannel.send(AddEditRefuelEvent.NavigateToHistoryWithResult(
            ADD_REFUEL_RESULT_OK))
    }

    sealed class AddEditRefuelEvent() {
        data class ShowInvalidInputMessage(val msg: String) : AddEditRefuelEvent()
        data class NavigateToHistoryWithResult(val result: Int) : AddEditRefuelEvent()
    }
}
