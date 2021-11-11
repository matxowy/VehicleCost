package com.matxowy.vehiclecost.ui.calculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.internal.SelectedTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    var currentTabSelected = SelectedTab.CONSUMPTION

    var refueled: Double? = null
    var kmTraveled: Double? = null
    var fuelPrice: Double? = null
    var avgFuelConsumption: Double? = null
    var numberOfPeople: Int? = null
    var paid: Double? = null


    val currentRefueled : MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val currentKmTraveled: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val currentFuelPrice: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val currentAvgFuelConsumption: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val currentNumberOfPeople: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val currentPaid: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    private val calculatorEventChannel = Channel<CalculatorEvent>()
    val calculatorEvent = calculatorEventChannel.receiveAsFlow()


    fun doCalculation() {
        when (currentTabSelected) {
            SelectedTab.CONSUMPTION -> {
                doCalculationInConsumptionTab()
            }
            SelectedTab.COSTS -> {
                doCalculationInCostsTab()
            }
            SelectedTab.RANGE -> {
                doCalculationInRangeTab()
            }
        }
    }

    private fun doCalculationInConsumptionTab() = viewModelScope.launch {
        if (refueled == null || kmTraveled == null || fuelPrice == null) {
            calculatorEventChannel.send(CalculatorEvent.ShowMessageAboutMissingData)
        } else {
            val avgConsumption = refueled!! / kmTraveled!! * 100
            val priceFor100km = avgConsumption * fuelPrice!!

            calculatorEventChannel.send(CalculatorEvent.ShowResultForConsumptionTab(avgConsumption, priceFor100km))
        }
    }

    private fun doCalculationInCostsTab() = viewModelScope.launch {
        if (avgFuelConsumption == null || kmTraveled == null || fuelPrice == null || numberOfPeople == null) {
            calculatorEventChannel.send(CalculatorEvent.ShowMessageAboutMissingData)
        } else {
            val requiredAmountOfFuel = (kmTraveled!! / 100) * avgFuelConsumption!!
            val costForTravel = fuelPrice!! * requiredAmountOfFuel
            val costPerPerson = costForTravel / numberOfPeople!!

            calculatorEventChannel.send(CalculatorEvent.ShowResultForCostsTab(requiredAmountOfFuel, costForTravel, costPerPerson))
        }
    }

    private fun doCalculationInRangeTab() = viewModelScope.launch {
        if (avgFuelConsumption == null || paid == null || fuelPrice == null) {
            calculatorEventChannel.send(CalculatorEvent.ShowMessageAboutMissingData)
        } else {
            val amountOfFilledWithFuel = paid!! / fuelPrice!!
            val rangeInKm = amountOfFilledWithFuel / avgFuelConsumption!! * 100

            calculatorEventChannel.send(CalculatorEvent.ShowResultForRangeTab(amountOfFilledWithFuel, rangeInKm))
        }
    }

    sealed class CalculatorEvent {
        object ShowMessageAboutMissingData : CalculatorEvent()
        data class ShowResultForConsumptionTab(val avgConsumption: Double, val price: Double) : CalculatorEvent()
        data class ShowResultForCostsTab(val requiredAmountOfFuel: Double, val costForTravel: Double, val costPerPerson: Double) : CalculatorEvent()
        data class ShowResultForRangeTab(val amountOfFilledWithFuel: Double, val rangeInKm: Double) : CalculatorEvent()
    }

}