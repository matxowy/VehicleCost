package com.matxowy.vehiclecost.ui.statistics

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StatisticsViewModel @ViewModelInject constructor(
    refuelDao: RefuelDao,
    repairDao: RepairDao
) : ViewModel() {

    private val statisticsChannel = Channel<StatisticsEvent>()
    val statisticsEvent = statisticsChannel.receiveAsFlow()

    fun onAddNewRefuelClick() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.NavigateToAddRefuelScreen)
    }

    fun onAddNewRepairClick() = viewModelScope.launch {
        statisticsChannel.send(StatisticsEvent.NavigateToAddRepairScreen)
    }

    // Refuels stats
    val sumOfFuelAmount = refuelDao.getSumOfRefuels().asLiveData()
    val sumCostsOfRefuels = refuelDao.getSumOfCosts().asLiveData()
    val lastPriceOfFuel = refuelDao.getLastPriceOfFuel().asLiveData()

    // Repairs stats
    val sumCostOfRepair = repairDao.getSumCostOfRepair().asLiveData()
    val maxCostOfRepair = repairDao.getMaxCostOfRepair().asLiveData()
    val lastCostOfRepair = repairDao.getLastCost().asLiveData()

    sealed class StatisticsEvent {
        object NavigateToAddRefuelScreen : StatisticsEvent()
        object NavigateToAddRepairScreen : StatisticsEvent()
    }
}