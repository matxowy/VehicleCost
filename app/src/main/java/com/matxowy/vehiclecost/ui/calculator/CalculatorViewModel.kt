package com.matxowy.vehiclecost.ui.calculator

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.internal.SelectedTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CalculatorViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    var currentTabSelected = SelectedTab.CONSUMPTION

    var refueled = state.get<Double>("refueled") ?: ""
        set(value) {
            field = value
            state.set("refueled", value)
        }

    var kmTraveled = state.get<Double>("kmTraveled") ?: ""
        set(value) {
            field = value
            state.set("kmTraveled", value)
        }

    var fuelPrice = state.get<Double>("fuelPrice") ?: ""
        set(value) {
            field = value
            state.set("fuelPrice", value)
        }

    var avgFuelConsumption = state.get<Double>("avgFuelConsumption") ?: ""
        set(value) {
            field = value
            state.set("avgFuelConsumption", value)
        }

    var numberOfPeople = state.get<Int>("numberOfPeople") ?: ""
        set(value) {
            field = value
            state.set("numberOfPeople", value)
        }

    var paid = state.get<Double>("paid") ?: ""
        set(value) {
            field = value
            state.set("paid", value)
        }

    /*var refueled: Double? = null
    var kmTraveled: Double? = null
    var fuelPrice: Double? = null
    var avgFuelConsumption: Double? = null
    var numberOfPeople: Int? = null
    var paid: Double? = null*/



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
        if (refueled.toString().isBlank() || kmTraveled.toString().isBlank() || fuelPrice.toString().isBlank()) {
            calculatorEventChannel.send(CalculatorEvent.ShowMessageAboutMissingData)
        } else {
            val avgConsumption = refueled.toString().toDouble() / kmTraveled.toString().toDouble() * 100
            val priceFor100km = avgConsumption * fuelPrice.toString().toDouble()

            calculatorEventChannel.send(CalculatorEvent.ShowResultForConsumptionTab(avgConsumption, priceFor100km))
        }
    }

    private fun doCalculationInCostsTab() = viewModelScope.launch {
        if (avgFuelConsumption.toString().isBlank() || kmTraveled.toString().isBlank() || fuelPrice.toString().isBlank() || numberOfPeople.toString().isBlank() ) {
            calculatorEventChannel.send(CalculatorEvent.ShowMessageAboutMissingData)
        } else {
            val requiredAmountOfFuel = (kmTraveled.toString().toDouble() / 100) * avgFuelConsumption.toString().toDouble()
            val costForTravel = fuelPrice.toString().toDouble() * requiredAmountOfFuel
            val costPerPerson = costForTravel / numberOfPeople.toString().toDouble()

            calculatorEventChannel.send(CalculatorEvent.ShowResultForCostsTab(requiredAmountOfFuel, costForTravel, costPerPerson))
        }
    }

    private fun doCalculationInRangeTab() = viewModelScope.launch {
        if (avgFuelConsumption.toString().isBlank() || paid.toString().isBlank() || fuelPrice.toString().isBlank()) {
            calculatorEventChannel.send(CalculatorEvent.ShowMessageAboutMissingData)
        } else {
            val amountOfFilledWithFuel = paid.toString().toDouble() / fuelPrice.toString().toDouble()
            val rangeInKm = amountOfFilledWithFuel / avgFuelConsumption.toString().toDouble() * 100

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
