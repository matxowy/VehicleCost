package com.matxowy.vehiclecost.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val refuelDao: RefuelDao,
    private val repairDao: RepairDao,
    private val vehicleDao: VehicleDao,
    @Named("IO") private val coroutineDispatcher: CoroutineDispatcher,
    val localPreferences: LocalPreferencesApi,
) : ViewModel() {

    private val refuelAndRepairEventChannel = Channel<HistoryEvent>()
    val refuelAndRepairEvent = refuelAndRepairEventChannel.receiveAsFlow()

    private val selectedVehicleId = localPreferences.getSelectedVehicleId()
    val refuels = refuelDao.getRefuels(selectedVehicleId).asLiveData()
    val repairs = repairDao.getRepairs(selectedVehicleId).asLiveData()

    fun onRefuelSwiped(refuel: Refuel) = viewModelScope.launch(coroutineDispatcher) {
        try {
            val currentVehicleMileage = vehicleDao.getVehicleMileageById(refuel.vehicleId).first()
            refuelDao.delete(refuel)
            if (refuel.mileage == currentVehicleMileage) {
                val newVehicleMileage = refuelDao.getLastMileage(refuel.vehicleId).first()
                vehicleDao.updateMileageOfVehicle(refuel.vehicleId, newVehicleMileage)
            }
            HistoryEvent.ShowUndoDeleteRefuelMessage(refuel).send()
        } catch (e: Exception) {
            HistoryEvent.ShowDefaultErrorMessage.send()
        }
    }

    fun onRepairSwiped(repair: Repair) = viewModelScope.launch(coroutineDispatcher) {
        try {
            repairDao.delete(repair)
            HistoryEvent.ShowUndoDeleteRepairMessage(repair).send()
        } catch (e: Exception) {
            HistoryEvent.ShowDefaultErrorMessage.send()
        }
    }

    fun onUndoDeleteRefuelClick(refuel: Refuel) = viewModelScope.launch(coroutineDispatcher) {
        try {
            val currentVehicleMileage = vehicleDao.getVehicleMileageById(refuel.vehicleId).first()
            refuelDao.insert(refuel)
            if (refuel.mileage > currentVehicleMileage) {
                vehicleDao.updateMileageOfVehicle(refuel.vehicleId, refuel.mileage)
            }
        } catch (e: Exception) {
            HistoryEvent.ShowDefaultErrorMessage.send()
        }
    }

    fun onUndoDeleteRepairClick(repair: Repair) = viewModelScope.launch(coroutineDispatcher) {
        try {
            repairDao.insert(repair)
        } catch (e: Exception) {
            HistoryEvent.ShowDefaultErrorMessage.send()
        }
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

    fun onAddNewRefuelClick() = HistoryEvent.NavigateToAddRefuelScreen.send()

    fun onAddNewRepairClick() = HistoryEvent.NavigateToAddRepairScreen.send()

    fun onRepairItemSelected(repair: Repair) = HistoryEvent.NavigateToEditRepairScreen(repair).send()

    fun onRefuelItemSelected(refuel: Refuel) = HistoryEvent.NavigateToEditRefuelScreen(refuel).send()

    private fun showRefuelSavedConfirmationMessage() = HistoryEvent.ShowRefuelSavedConfirmationMessage.send()

    private fun showRefuelEditedConfirmationMessage() = HistoryEvent.ShowRefuelEditedConfirmationMessage.send()

    private fun showRepairSavedConfirmationMessage() = HistoryEvent.ShowRepairSavedConfirmationMessage.send()

    private fun showRepairEditedConfirmationMessage() = HistoryEvent.ShowRepairEditedConfirmationMessage.send()

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

    private fun HistoryEvent.send() {
        viewModelScope.launch { refuelAndRepairEventChannel.send(this@send) }
    }
}