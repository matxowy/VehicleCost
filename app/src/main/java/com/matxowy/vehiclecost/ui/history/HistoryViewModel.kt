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

    private val refuelAndRepairEventChannel = Channel<RefuelAndRepairEvent>()
    val refuelAndRepairEvent = refuelAndRepairEventChannel.receiveAsFlow()

    fun onAddNewRefuelClick() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.NavigateToAddRefuelScreen)
    }

    fun onAddNewRepairClick() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.NavigateToAddRepairScreen)
    }

    fun onRefuelSwiped(refuel: Refuel) = viewModelScope.launch {
        refuelDao.delete(refuel)
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowUndoDeleteRefuelMessage(refuel))
    }

    fun onRepairSwiped(repair: Repair) = viewModelScope.launch {
        repairDao.delete(repair)
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowUndoDeleteRepairMessage(repair))
    }

    fun onUndoDeleteRefuelClick(refuel: Refuel) = viewModelScope.launch {
        refuelDao.insert(refuel)
    }

    fun onUndoDeleteRepairClick(repair: Repair) = viewModelScope.launch {
        repairDao.insert(repair)
    }

    fun onRepairItemSelected(repair: Repair) = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.NavigateToEditRepairScreen(repair))
    }

    fun onRefuelItemSelected(refuel: Refuel) = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.NavigateToEditRefuelScreen(refuel))
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
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowRefuelSavedConfirmationMessage)
    }

    private fun showRefuelEditedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowRefuelEditedConfirmationMessage)
    }

    private fun showRepairSavedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowRepairSavedConfirmationMessage)
    }

    private fun showRepairEditedConfirmationMessage() = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowRepairEditedConfirmationMessage)
    }

    private val selectedVehicleId = localPreferences.getSelectedVehiclePosition()
    val refuels = refuelDao.getRefuels(selectedVehicleId).asLiveData()
    val repairs = repairDao.getRepairs(selectedVehicleId).asLiveData()

    sealed class RefuelAndRepairEvent {
        object NavigateToAddRefuelScreen : RefuelAndRepairEvent()
        object NavigateToAddRepairScreen : RefuelAndRepairEvent()
        object ShowRefuelSavedConfirmationMessage : RefuelAndRepairEvent()
        object ShowRefuelEditedConfirmationMessage : RefuelAndRepairEvent()
        object ShowRepairSavedConfirmationMessage : RefuelAndRepairEvent()
        object ShowRepairEditedConfirmationMessage : RefuelAndRepairEvent()
        data class NavigateToEditRefuelScreen(val refuel: Refuel) : RefuelAndRepairEvent()
        data class NavigateToEditRepairScreen(val repair: Repair) : RefuelAndRepairEvent()
        data class ShowUndoDeleteRefuelMessage(val refuel: Refuel) : RefuelAndRepairEvent()
        data class ShowUndoDeleteRepairMessage(val repair: Repair) : RefuelAndRepairEvent()
    }
}