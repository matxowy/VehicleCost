package com.matxowy.vehiclecost.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val refuelDao: RefuelDao,
    private val repairDao: RepairDao,
    val localPreferences: LocalPreferencesApi,
) : ViewModel() {

    private val refuelAndRepairEventChannel = Channel<HistoryEvent>()
    val refuelAndRepairEvent = refuelAndRepairEventChannel.receiveAsFlow()

    fun onAddNewRefuelClick() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.NavigateToAddRefuelScreen)
    }

    fun onAddNewRepairClick() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.NavigateToAddRepairScreen)
    }

    fun onRefuelSwiped(refuel: Refuel) = viewModelScope.launch {
        try {
            refuelDao.delete(refuel)
            refuelAndRepairEventChannel.send(HistoryEvent.ShowUndoDeleteRefuelMessage(refuel))
        } catch (e: Exception) {
            refuelAndRepairEventChannel.send(HistoryEvent.ShowDefaultErrorMessage)
        }
    }

    fun onRepairSwiped(repair: Repair) = viewModelScope.launch {
        try {
            repairDao.delete(repair)
            refuelAndRepairEventChannel.send(HistoryEvent.ShowUndoDeleteRepairMessage(repair))
        } catch (e: Exception) {
            refuelAndRepairEventChannel.send(HistoryEvent.ShowDefaultErrorMessage)
        }
    }

    fun onUndoDeleteRefuelClick(refuel: Refuel) = viewModelScope.launch {
        try {
            refuelDao.insert(refuel)
        } catch (e: Exception) {
            refuelAndRepairEventChannel.send(HistoryEvent.ShowDefaultErrorMessage)
        }
    }

    fun onUndoDeleteRepairClick(repair: Repair) = viewModelScope.launch {
        try {
            repairDao.insert(repair)
        } catch (e: Exception) {
            refuelAndRepairEventChannel.send(HistoryEvent.ShowDefaultErrorMessage)
        }
    }

    fun onRepairItemSelected(repair: Repair) = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.NavigateToEditRepairScreen(repair))
    }

    fun onRefuelItemSelected(refuel: Refuel) = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.NavigateToEditRefuelScreen(refuel))
    }

    fun onAddEditRefuelResult(result: Int) {
        when (result) {
            ADD_RESULT_OK -> showRefuelSavedConfirmationMessage()
            EDIT_RESULT_OK -> showRefuelEditedConfirmationMessage()
        }
    }

    fun onAddEditRepairResult(result: Int) {
        when (result) {
            ADD_RESULT_OK -> showRepairSavedConfirmationMessage()
            EDIT_RESULT_OK -> showRepairEditedConfirmationMessage()
        }
    }

    private fun showRefuelSavedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.ShowRefuelSavedConfirmationMessage)
    }

    private fun showRefuelEditedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.ShowRefuelEditedConfirmationMessage)
    }

    private fun showRepairSavedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.ShowRepairSavedConfirmationMessage)
    }

    private fun showRepairEditedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(HistoryEvent.ShowRepairEditedConfirmationMessage)
    }

    private val selectedVehicleId = localPreferences.getSelectedVehicleId()
    val refuels = refuelDao.getRefuels(selectedVehicleId).asLiveData()
    val repairs = repairDao.getRepairs(selectedVehicleId).asLiveData()

    sealed class HistoryEvent {
        object NavigateToAddRefuelScreen : HistoryEvent()
        object NavigateToAddRepairScreen : HistoryEvent()
        object ShowDefaultErrorMessage : HistoryEvent()
        object ShowRefuelSavedConfirmationMessage : HistoryEvent()
        object ShowRefuelEditedConfirmationMessage : HistoryEvent()
        object ShowRepairSavedConfirmationMessage : HistoryEvent()
        object ShowRepairEditedConfirmationMessage : HistoryEvent()
        data class NavigateToEditRefuelScreen(val refuel: Refuel) : HistoryEvent()
        data class NavigateToEditRepairScreen(val repair: Repair) : HistoryEvent()
        data class ShowUndoDeleteRefuelMessage(val refuel: Refuel) : HistoryEvent()
        data class ShowUndoDeleteRepairMessage(val repair: Repair) : HistoryEvent()
    }
}