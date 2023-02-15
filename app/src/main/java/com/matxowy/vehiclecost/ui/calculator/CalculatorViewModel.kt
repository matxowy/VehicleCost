package com.matxowy.vehiclecost.ui.calculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.vehiclecost.internal.SelectedTab
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_DOUBLE_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.hasDefaultValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    var currentTabSelected = SelectedTab.CONSUMPTION

    var refueled = state.get<Double>(REFUELED_STATE_KEY) ?: DEFAULT_DOUBLE_VALUE
        set(value) {
            field = value
            state[REFUELED_STATE_KEY] = value
        }

    var kmTraveled = state.get<Double>(KM_TRAVELED_STATE_KEY) ?: DEFAULT_DOUBLE_VALUE
        set(value) {
            field = value
            state[KM_TRAVELED_STATE_KEY] = value
        }

    var fuelPrice = state.get<Double>(FUEL_PRICE_STATE_KEY) ?: DEFAULT_DOUBLE_VALUE
        set(value) {
            field = value
            state[FUEL_PRICE_STATE_KEY] = value
        }

    var avgFuelConsumption = state.get<Double>(AVG_FUEL_CONSUMPTION_STATE_KEY) ?: DEFAULT_DOUBLE_VALUE
        set(value) {
            field = value
            state[AVG_FUEL_CONSUMPTION_STATE_KEY] = value
        }

    var numberOfPeople = state.get<Int>(NUMBER_OF_PEOPLE_STATE_KEY) ?: DEFAULT_INT_VALUE
        set(value) {
            field = value
            state[NUMBER_OF_PEOPLE_STATE_KEY] = value
        }

    var paid = state.get<Double>(PAID_STATE_KEY) ?: DEFAULT_DOUBLE_VALUE
        set(value) {
            field = value
            state[PAID_STATE_KEY] = value
        }

    val currentRefueled: MutableLiveData<Double> by lazy {
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

    private fun doCalculationInConsumptionTab() {
        if (refueled.hasDefaultValue() || kmTraveled.hasDefaultValue() || fuelPrice.hasDefaultValue()) {
            CalculatorEvent.ShowMessageAboutMissingData.send()
        } else {
            val avgConsumption = refueled / kmTraveled * 100
            val priceFor100km = avgConsumption * fuelPrice

            CalculatorEvent.ShowResultForConsumptionTab(avgConsumption, priceFor100km).send()
        }
    }

    private fun doCalculationInCostsTab() {
        if (avgFuelConsumption.hasDefaultValue() || kmTraveled.hasDefaultValue() || fuelPrice.hasDefaultValue() || numberOfPeople.hasDefaultValue()) {
            CalculatorEvent.ShowMessageAboutMissingData.send()
        } else {
            val requiredAmountOfFuel = (kmTraveled / 100) * avgFuelConsumption
            val costForTravel = fuelPrice * requiredAmountOfFuel
            val costPerPerson = costForTravel / numberOfPeople.toDouble()

            CalculatorEvent.ShowResultForCostsTab(requiredAmountOfFuel, costForTravel, costPerPerson).send()
        }
    }

    private fun doCalculationInRangeTab() {
        if (avgFuelConsumption.hasDefaultValue() || paid.hasDefaultValue() || fuelPrice.hasDefaultValue()) {
            CalculatorEvent.ShowMessageAboutMissingData.send()
        } else {
            val amountOfFilledWithFuel = paid / fuelPrice
            val rangeInKm = amountOfFilledWithFuel / avgFuelConsumption * 100

            CalculatorEvent.ShowResultForRangeTab(amountOfFilledWithFuel, rangeInKm).send()
        }
    }

    companion object {
        const val REFUELED_STATE_KEY = "refueled"
        const val KM_TRAVELED_STATE_KEY = "kmTraveled"
        const val FUEL_PRICE_STATE_KEY = "fuelPrice"
        const val AVG_FUEL_CONSUMPTION_STATE_KEY = "avgFuelConsumption"
        const val NUMBER_OF_PEOPLE_STATE_KEY = "numberOfPeople"
        const val PAID_STATE_KEY = "paid"
    }

    sealed class CalculatorEvent {
        object ShowMessageAboutMissingData : CalculatorEvent()
        data class ShowResultForConsumptionTab(val avgConsumption: Double, val price: Double) : CalculatorEvent()
        data class ShowResultForCostsTab(val requiredAmountOfFuel: Double, val costForTravel: Double, val costPerPerson: Double) : CalculatorEvent()
        data class ShowResultForRangeTab(val amountOfFilledWithFuel: Double, val rangeInKm: Double) : CalculatorEvent()
    }

    private fun CalculatorEvent.send() {
        viewModelScope.launch { calculatorEventChannel.send(this@send) }
    }
}
