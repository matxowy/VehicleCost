package com.matxowy.vehiclecost.ui.addeditrepair

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class AddEditRepairViewModel @ViewModelInject constructor(
    private val repairDao: RepairDao,
    @Assisted private val state: SavedStateHandle,
    @ApplicationContext private val context: Context,
    val localPreferences: LocalPreferencesApi,
) : ViewModel() {
    val repair = state.get<Repair>("repair")

    var title = state.get<String>("repairTitle") ?: repair?.title ?: ""
        set(value) {
            field = value
            state.set("repairTitle", value)
        }

    var mileage = state.get<Int>("repairMileage") ?: repair?.mileage ?: ""
        set(value) {
            field = value
            state.set("repairMileage", value)
        }

    var cost = state.get<Double>("repairCost") ?: repair?.cost ?: ""
        set(value) {
            field = value
            state.set("repairCost", value)
        }

    var date = state.get<String>("repairDate") ?: repair?.date ?: LocalDateConverter.dateToString(
        LocalDate.now()
    )
        set(value) {
            field = value
            state.set("repairDate", value)
        }

    var time = state.get<String>("repairTime") ?: repair?.time ?: LocalDateConverter.timeToString(
        LocalDateTime.now()
    )
        set(value) {
            field = value
            state.set("repairTime", value)
        }

    var comments = state.get<String>("repairComments") ?: repair?.comments ?: ""
        set(value) {
            field = value
            state.set("repairComments", value)
        }

    private val addEditRepairEventChannel = Channel<AddEditRepairEvent>()
    val addEditRepairEvent = addEditRepairEventChannel.receiveAsFlow()

    private val selectedVehicleId = localPreferences.getSelectedVehicleId()
    var lastMileage = repairDao.getLastMileage(selectedVehicleId).asLiveData()

    fun onSaveRepairClick() {
        if (title.isBlank()
            || cost.toString().isBlank()
            || mileage.toString().isBlank()
        ) {
            showInvalidInputMessage(context.getString(R.string.required_fields_cannot_be_empty_text))
            return
        }

        // The new mileage can't be less than previous
        if (repair == null) {
            lastMileage.value?.let { lastMileage ->
                if (mileage.toString().toInt() <= lastMileage) {
                    showInvalidInputMessage(context.getString(R.string.mileage_cannot_be_less_than_previous))
                    return
                }
            }
        }

        if (repair != null) {
            val updatedRepair = repair.copy(
                title = title,
                mileage = mileage.toString().toInt(),
                cost = cost.toString().toDouble(),
                date = date.toString(),
                time = time.toString(),
                comments = comments,
                vehicleId = selectedVehicleId,
            )

            updateRepair(updatedRepair)
        } else {
            val repair = Repair(
                title = title,
                mileage = mileage.toString().toInt(),
                cost = cost.toString().toDouble(),
                date = date.toString(),
                time = time.toString(),
                comments = comments,
                vehicleId = selectedVehicleId,
            )

            createRepair(repair)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditRepairEventChannel.send(AddEditRepairEvent.ShowInvalidDataMessage(text))
    }

    private fun createRepair(repair: Repair) = viewModelScope.launch {
        repairDao.insert(repair)
        addEditRepairEventChannel.send(AddEditRepairEvent.NavigateToHistoryWithResult(ADD_RESULT_OK))
    }

    private fun updateRepair(updatedRepair: Repair) = viewModelScope.launch {
        repairDao.update(updatedRepair)
        addEditRepairEventChannel.send(AddEditRepairEvent.NavigateToHistoryWithResult(EDIT_RESULT_OK))
    }

    sealed class AddEditRepairEvent {
        data class ShowInvalidDataMessage(val msg: String) : AddEditRepairEvent()
        data class NavigateToHistoryWithResult(val result: Int) : AddEditRepairEvent()
    }

}
