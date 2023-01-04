package com.matxowy.vehiclecost.ui.history

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.ui.ADD_REFUEL_RESULT_OK
import com.matxowy.vehiclecost.ui.ADD_REPAIR_RESULT_OK
import com.matxowy.vehiclecost.ui.EDIT_REFUEL_RESULT_OK
import com.matxowy.vehiclecost.ui.EDIT_REPAIR_RESULT_OK
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HistoryViewModel @ViewModelInject constructor(
    private val refuelDao: RefuelDao,
    private val repairDao: RepairDao,
    @ApplicationContext private val context: Context,
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
            ADD_REFUEL_RESULT_OK -> showRefuelSavedConfirmationMessage(context.getString(R.string.refueled_added_message))
            EDIT_REFUEL_RESULT_OK -> showRefuelSavedConfirmationMessage(context.getString(R.string.refueled_updated_message))
        }
    }

    fun onAddEditRepairResult(result: Int) {
        when (result) {
            ADD_REPAIR_RESULT_OK -> showRepairSavedConfirmationMessage(context.getString(R.string.repair_added_message))
            EDIT_REPAIR_RESULT_OK -> showRepairSavedConfirmationMessage(context.getString(R.string.repair_updated_message))
        }
    }

    private fun showRefuelSavedConfirmationMessage(text: String) = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowRefuelSavedConfirmationMessage(text))
    }

    private fun showRepairSavedConfirmationMessage(text: String) = viewModelScope.launch {
        refuelAndRepairEventChannel.send(RefuelAndRepairEvent.ShowRepairSavedConfirmationMessage(text))
    }

    private val selectedVehicleId = localPreferences.getSelectedVehicleId()
    val refuels = refuelDao.getRefuels(selectedVehicleId).asLiveData()
    val repairs = repairDao.getRepairs(selectedVehicleId).asLiveData()

    sealed class RefuelAndRepairEvent {
        object NavigateToAddRefuelScreen : RefuelAndRepairEvent()
        object NavigateToAddRepairScreen : RefuelAndRepairEvent()
        data class NavigateToEditRefuelScreen(val refuel: Refuel) : RefuelAndRepairEvent()
        data class NavigateToEditRepairScreen(val repair: Repair) : RefuelAndRepairEvent()
        data class ShowUndoDeleteRefuelMessage(val refuel: Refuel) : RefuelAndRepairEvent()
        data class ShowUndoDeleteRepairMessage(val repair: Repair) : RefuelAndRepairEvent()
        data class ShowRefuelSavedConfirmationMessage(val msg: String) : RefuelAndRepairEvent()
        data class ShowRepairSavedConfirmationMessage(val msg: String) : RefuelAndRepairEvent()
    }
}