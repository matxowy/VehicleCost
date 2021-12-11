package com.matxowy.vehiclecost.ui.history

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HistoryViewModel @ViewModelInject constructor(
    private val refuelDao: RefuelDao,
    private val repairDao: RepairDao
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

    val refuels = refuelDao.getRefuels().asLiveData()
    val repairs = repairDao.getRepairs().asLiveData()

    sealed class RefuelAndRepairEvent {
        object NavigateToAddRefuelScreen : RefuelAndRepairEvent()
        data class NavigateToEditRefuelScreen(val refuel: Refuel) : RefuelAndRepairEvent()
        object NavigateToAddRepairScreen : RefuelAndRepairEvent()
        data class NavigateToEditRepairScreen(val repair: Repair) : RefuelAndRepairEvent()
        data class ShowUndoDeleteRefuelMessage(val refuel: Refuel) : RefuelAndRepairEvent()
        data class ShowUndoDeleteRepairMessage(val repair: Repair) : RefuelAndRepairEvent()
    }
}