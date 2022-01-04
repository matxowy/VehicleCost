package com.matxowy.vehiclecost.ui.addeditrepair

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.ui.ADD_REPAIR_RESULT_OK
import com.matxowy.vehiclecost.ui.EDIT_REPAIR_RESULT_OK
import com.matxowy.vehiclecost.util.LocalDateConverter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class AddEditRepairViewModel @ViewModelInject constructor(
    private val repairDao: RepairDao,
    @Assisted private val state: SavedStateHandle
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

    var lastMileage = repairDao.getLastMileage().asLiveData()

    fun onSaveRepairClick() {
        if (title.isBlank()
            || cost.toString().isBlank()
            || mileage.toString().isBlank()
        ) {
            showInvalidInputMessage("Wymagane pola nie mogą być puste")
            return
        }

        if (repair != null) {
            val updatedRepair = repair.copy(
                title = title,
                mileage = mileage.toString().toInt(),
                cost = cost.toString().toDouble(),
                date = date.toString(),
                time = time.toString(),
                comments = comments
            )

            updateRepair(updatedRepair)
        } else {
            val repair = Repair(
                title = title,
                mileage = mileage.toString().toInt(),
                cost = cost.toString().toDouble(),
                date = date.toString(),
                time = time.toString(),
                comments = comments
            )

            createRepair(repair)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditRepairEventChannel.send(AddEditRepairEvent.ShowInvalidDataMessage(text))
    }

    private fun createRepair(repair: Repair) = viewModelScope.launch {
        repairDao.insert(repair)
        addEditRepairEventChannel.send(
            AddEditRepairEvent.NavigateToHistoryWithResult(
                ADD_REPAIR_RESULT_OK
            )
        )
    }

    private fun updateRepair(updatedRepair: Repair) = viewModelScope.launch {
        repairDao.update(updatedRepair)
        addEditRepairEventChannel.send(
            AddEditRepairEvent.NavigateToHistoryWithResult(
                EDIT_REPAIR_RESULT_OK
            )
        )
    }

    sealed class AddEditRepairEvent() {
        data class ShowInvalidDataMessage(val msg: String) : AddEditRepairEvent()
        data class NavigateToHistoryWithResult(val result: Int) : AddEditRepairEvent()
    }

}
