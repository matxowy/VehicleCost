package com.matxowy.vehiclecost.ui.addeditrepair

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_DOUBLE_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_STRING_VALUE
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import com.matxowy.vehiclecost.util.hasDefaultValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AddEditRepairViewModel @Inject constructor(
    private val repairDao: RepairDao,
    private val state: SavedStateHandle,
    @Named("IO") private val coroutineDispatcher: CoroutineDispatcher,
    val localPreferences: LocalPreferencesApi,
) : ViewModel() {
    val repair = state.get<Repair>(REPAIR_STATE_KEY)

    var title = state.get<String>(TITLE_STATE_KEY) ?: repair?.title ?: DEFAULT_STRING_VALUE
        set(value) {
            field = value
            state[TITLE_STATE_KEY] = value
        }

    var mileage = state.get<Int>(MILEAGE_STATE_KEY) ?: repair?.mileage ?: DEFAULT_INT_VALUE
        set(value) {
            field = value
            state[MILEAGE_STATE_KEY] = value
        }

    var cost = state.get<Double>(COST_STATE_KEY) ?: repair?.cost ?: DEFAULT_DOUBLE_VALUE
        set(value) {
            field = value
            state[COST_STATE_KEY] = value
        }

    var date = state.get<String>(DATE_STATE_KEY) ?: repair?.date ?: LocalDateConverter.dateToString(LocalDate.now())
        set(value) {
            field = value
            state[DATE_STATE_KEY] = value
        }

    var time = state.get<String>(TIME_STATE_KEY) ?: repair?.time ?: LocalDateConverter.timeToString(LocalDateTime.now())
        set(value) {
            field = value
            state[TIME_STATE_KEY] = value
        }

    var comments = state.get<String>(COMMENTS_STATE_KEY) ?: repair?.comments ?: DEFAULT_STRING_VALUE
        set(value) {
            field = value
            state[COMMENTS_STATE_KEY] = value
        }

    private val addEditRepairEventChannel = Channel<AddEditRepairEvent>()
    val addEditRepairEvent = addEditRepairEventChannel.receiveAsFlow()

    private val selectedVehicleId = localPreferences.getSelectedVehicleId()

    fun onSaveRepairClick() {
        if (title.isBlank()
            || cost.hasDefaultValue()
            || mileage.hasDefaultValue()
        ) {
            showFieldsCannotBeEmptyMessage()
            return
        }

        if (repair != null) {
            val updatedRepair = repair.copy(
                title = title,
                mileage = mileage,
                cost = cost,
                date = date.toString(),
                time = time.toString(),
                comments = comments,
                vehicleId = selectedVehicleId,
            )
            updateRepair(updatedRepair)
        } else {
            val repair = Repair(
                title = title,
                mileage = mileage,
                cost = cost,
                date = date.toString(),
                time = time.toString(),
                comments = comments,
                vehicleId = selectedVehicleId,
            )
            createRepair(repair)
        }
    }

    private fun showFieldsCannotBeEmptyMessage() = AddEditRepairEvent.ShowFieldsCannotBeEmptyMessage.send()

    private fun createRepair(repair: Repair) = viewModelScope.launch(coroutineDispatcher) {
        try {
            repairDao.insert(repair)
            AddEditRepairEvent.NavigateToHistoryWithResult(ADD_RESULT_OK).send()
        } catch (e: Exception) {
            AddEditRepairEvent.ShowDefaultErrorMessage.send()
        }
    }

    private fun updateRepair(updatedRepair: Repair) = viewModelScope.launch(coroutineDispatcher) {
        try {
            repairDao.update(updatedRepair)
            AddEditRepairEvent.NavigateToHistoryWithResult(EDIT_RESULT_OK).send()
        } catch (e: Exception) {
            AddEditRepairEvent.ShowDefaultErrorMessage.send()
        }
    }

    companion object {
        const val REPAIR_STATE_KEY = "repair"
        const val TITLE_STATE_KEY = "repairTitle"
        const val MILEAGE_STATE_KEY = "repairMileage"
        const val DATE_STATE_KEY = "repairDate"
        const val TIME_STATE_KEY = "repairTime"
        const val COST_STATE_KEY = "repairCost"
        const val COMMENTS_STATE_KEY = "repairComments"
    }

    sealed class AddEditRepairEvent {
        object ShowFieldsCannotBeEmptyMessage : AddEditRepairEvent()
        object ShowDefaultErrorMessage : AddEditRepairEvent()
        data class NavigateToHistoryWithResult(val result: Int) : AddEditRepairEvent()
    }

    private fun AddEditRepairEvent.send() {
        viewModelScope.launch { addEditRepairEventChannel.send(this@send) }
    }
}
