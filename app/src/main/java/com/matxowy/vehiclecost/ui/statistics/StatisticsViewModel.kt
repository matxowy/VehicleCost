package com.matxowy.vehiclecost.ui.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val refuelDao: RefuelDao,
    val repairDao: RepairDao,
    val vehicleDao: VehicleDao,
    private val localPreferences: LocalPreferencesApi,
) : ViewModel() {

    private val statisticsChannel = Channel<StatisticsEvent>()
    val statisticsEvent = statisticsChannel.receiveAsFlow()

    // Refuels stats
    val sumOfFuelAmount: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>(DEFAULT_STATISTICS_VALUE)
    }
    val sumCostsOfRefuels: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>(DEFAULT_STATISTICS_VALUE)
    }
    val lastPriceOfFuel: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>(DEFAULT_STATISTICS_VALUE)
    }

    // Repairs stats
    val sumCostOfRepair: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>(DEFAULT_STATISTICS_VALUE)
    }
    val maxCostOfRepair: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>(DEFAULT_STATISTICS_VALUE)
    }
    val lastCostOfRepair: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>(DEFAULT_STATISTICS_VALUE)
    }

    init {
        refreshStatistics()
    }

    // Vehicle cars
    val vehiclesNames = vehicleDao.getVehiclesNames().asLiveData()

    fun onAddNewRefuelClick() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.NavigateToAddRefuelScreen)
    }

    fun onAddNewRepairClick() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.NavigateToAddRepairScreen)
    }

    fun onAddNewVehicleClick() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.NavigateToAddEditVehicleScreen)
    }

    fun saveSelectedVehicleAndRefreshStatistics(position: Int, vehicleName: String) {
        if (position != 0) {
            localPreferences.saveSelectedVehiclePosition(position)
            localPreferences.saveSelectedVehicleName(vehicleName)
            refreshStatistics()
        }
    }

    fun getSelectedVehicleId() = localPreferences.getSelectedVehiclePosition()

    private fun refreshStatistics() {
        val selectedVehicleId = getSelectedVehicleId()

        viewModelScope.launch(Dispatchers.IO) {
            sumOfFuelAmount.postValue(refuelDao.getSumOfRefuels(selectedVehicleId))
            sumCostsOfRefuels.postValue(refuelDao.getSumOfCosts(selectedVehicleId))
            lastPriceOfFuel.postValue(refuelDao.getLastPriceOfFuel(selectedVehicleId))

            sumCostOfRepair.postValue(repairDao.getSumCostOfRepair(selectedVehicleId))
            maxCostOfRepair.postValue(repairDao.getMaxCostOfRepair(selectedVehicleId))
            lastCostOfRepair.postValue(repairDao.getLastCost(selectedVehicleId))
        }
    }

    fun getListOfVehiclesNamesWithAddingOptionOnFirstPosition(listOfVehiclesNames: List<String>, addNewVehicleText: String): List<String> {
        val newList = listOfVehiclesNames.toMutableList()
        newList.add(0, addNewVehicleText)
        return newList
    }

    fun onAddEditVehicleResult(result: Int) {
        when (result) {
            ADD_RESULT_OK -> showVehicleAddedConfirmationMessage()
            EDIT_RESULT_OK -> showVehicleEditedConfirmationMessage()
        }
    }

    private fun showVehicleEditedConfirmationMessage() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.ShowVehicleSavedConfirmationMessage)
    }

    private fun showVehicleAddedConfirmationMessage() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.ShowVehicleEditedConfirmationMessage)
    }

    sealed class StatisticsEvent {
        object NavigateToAddRefuelScreen : StatisticsEvent()
        object NavigateToAddRepairScreen : StatisticsEvent()
        object NavigateToAddEditVehicleScreen : StatisticsEvent()
        object ShowVehicleSavedConfirmationMessage : StatisticsEvent()
        object ShowVehicleEditedConfirmationMessage : StatisticsEvent()
    }

    companion object {
        const val DEFAULT_STATISTICS_VALUE = 0.0
    }
}