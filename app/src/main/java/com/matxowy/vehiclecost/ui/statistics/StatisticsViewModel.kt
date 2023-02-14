package com.matxowy.vehiclecost.ui.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val refuelDao: RefuelDao,
    val repairDao: RepairDao,
    val vehicleDao: VehicleDao,
    @Named("IO") private val coroutineDispatcher: CoroutineDispatcher,
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

    val vehiclesNames = vehicleDao.getVehicles().asLiveData()

    init {
        refreshStatistics()
    }

    fun getSelectedVehiclePosition() = localPreferences.getSelectedVehiclePosition()

    fun saveSelectedVehicleAndRefreshStatistics(position: Int, vehicleId: Int) {
        if (position != 0) {
            localPreferences.apply {
                saveSelectedVehiclePosition(position)
                saveSelectedVehicleId(vehicleId)
            }
            refreshStatistics()
        }
    }

    fun getListOfVehiclesNamesWithAddingOptionOnFirstPosition(listOfVehiclesNames: List<Vehicle>, addNewVehicleText: String): List<Vehicle> {
        val newList = listOfVehiclesNames.toMutableList()
        newList.add(0, Vehicle(name = addNewVehicleText))
        return newList
    }

    fun onAddVehicleResult(result: Int) {
        when (result) {
            ADD_RESULT_OK -> showVehicleAddedConfirmationMessage()
        }
    }

    fun onAddNewRefuelClick() = StatisticsEvent.NavigateToAddRefuelScreen.send()

    fun onAddNewRepairClick() = StatisticsEvent.NavigateToAddRepairScreen.send()

    fun onAddNewVehicleClick() = StatisticsEvent.NavigateToAddVehicleScreen.send()

    private fun showVehicleAddedConfirmationMessage() = StatisticsEvent.ShowVehicleSavedConfirmationMessage.send()

    private fun refreshStatistics() {
        val selectedVehicleId = localPreferences.getSelectedVehicleId()

        viewModelScope.launch(coroutineDispatcher) {
            sumOfFuelAmount.postValue(refuelDao.getSumOfRefuels(selectedVehicleId))
            sumCostsOfRefuels.postValue(refuelDao.getSumOfCosts(selectedVehicleId))
            lastPriceOfFuel.postValue(refuelDao.getLastPriceOfFuel(selectedVehicleId))

            sumCostOfRepair.postValue(repairDao.getSumCostOfRepair(selectedVehicleId))
            maxCostOfRepair.postValue(repairDao.getMaxCostOfRepair(selectedVehicleId))
            lastCostOfRepair.postValue(repairDao.getLastCost(selectedVehicleId))
        }
    }

    sealed class StatisticsEvent {
        object NavigateToAddRefuelScreen : StatisticsEvent()
        object NavigateToAddRepairScreen : StatisticsEvent()
        object NavigateToAddVehicleScreen : StatisticsEvent()
        object ShowVehicleSavedConfirmationMessage : StatisticsEvent()
    }

    private fun StatisticsEvent.send() {
        viewModelScope.launch { statisticsChannel.send(this@send) }
    }

    companion object {
        const val DEFAULT_STATISTICS_VALUE = 0.0
    }
}