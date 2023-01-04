package com.matxowy.vehiclecost.ui.statistics

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StatisticsViewModel @ViewModelInject constructor(
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

    fun saveSelectedVehicleAndRefreshStatistics(position: Int) {
        localPreferences.saveSelectedVehiclePosition(position)
        refreshStatistics()
    }

    fun getSelectedVehiclePosition() = localPreferences.getSelectedVehiclePosition()

    private fun refreshStatistics() {
        val selectedVehicleId = localPreferences.getSelectedVehicleId()

        viewModelScope.launch(Dispatchers.IO) {
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
    }

    companion object {
        const val DEFAULT_STATISTICS_VALUE = 0.0
    }
}