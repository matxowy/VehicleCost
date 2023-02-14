package com.matxowy.vehiclecost.ui.calculator

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.matxowy.vehiclecost.internal.SelectedTab
import com.matxowy.vehiclecost.ui.calculator.CalculatorViewModel.Companion.AVG_FUEL_CONSUMPTION_STATE_KEY
import com.matxowy.vehiclecost.ui.calculator.CalculatorViewModel.Companion.FUEL_PRICE_STATE_KEY
import com.matxowy.vehiclecost.ui.calculator.CalculatorViewModel.Companion.KM_TRAVELED_STATE_KEY
import com.matxowy.vehiclecost.ui.calculator.CalculatorViewModel.Companion.NUMBER_OF_PEOPLE_STATE_KEY
import com.matxowy.vehiclecost.ui.calculator.CalculatorViewModel.Companion.PAID_STATE_KEY
import com.matxowy.vehiclecost.ui.calculator.CalculatorViewModel.Companion.REFUELED_STATE_KEY
import com.matxowy.vehiclecost.util.MainCoroutineRule
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CalculatorViewModelTest {
    private var mockkSavedStateHandle = SavedStateHandle(
        mapOf(
            REFUELED_STATE_KEY to REFUELED,
            KM_TRAVELED_STATE_KEY to KM_TRAVELED,
            FUEL_PRICE_STATE_KEY to FUEL_PRICE,
            AVG_FUEL_CONSUMPTION_STATE_KEY to AVG_FUEL_CONSUMPTION,
            NUMBER_OF_PEOPLE_STATE_KEY to NUMBER_OF_PEOPLE,
            PAID_STATE_KEY to PAID,
        )
    )
    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private var systemUnderTest = CalculatorViewModel(mockkSavedStateHandle)

    companion object {
        const val REFUELED = 25.0
        const val KM_TRAVELED = 250.0
        const val FUEL_PRICE = 7.5
        const val AVG_FUEL_CONSUMPTION = 10.0
        const val NUMBER_OF_PEOPLE = 4
        const val PAID = 187.5
    }

    @Test
    fun `when state is saved then fields should be filled with these values`() {
        systemUnderTest.apply {
            refueled shouldBe REFUELED
            kmTraveled shouldBe KM_TRAVELED
            fuelPrice shouldBe FUEL_PRICE
            avgFuelConsumption shouldBe AVG_FUEL_CONSUMPTION
            numberOfPeople shouldBe NUMBER_OF_PEOPLE
            paid shouldBe PAID
        }
    }

    @Test
    fun `when state is not saved then fields should be filled with default values`() {
        mockkSavedStateHandle[REFUELED_STATE_KEY] = null
        mockkSavedStateHandle[KM_TRAVELED_STATE_KEY] = null
        mockkSavedStateHandle[FUEL_PRICE_STATE_KEY] = null
        mockkSavedStateHandle[AVG_FUEL_CONSUMPTION_STATE_KEY] = null
        mockkSavedStateHandle[NUMBER_OF_PEOPLE_STATE_KEY] = null
        mockkSavedStateHandle[PAID_STATE_KEY] = null

        systemUnderTest = CalculatorViewModel(mockkSavedStateHandle)

        systemUnderTest.apply {
            refueled shouldBe ""
            kmTraveled shouldBe ""
            fuelPrice shouldBe ""
            avgFuelConsumption shouldBe ""
            numberOfPeople shouldBe ""
            paid shouldBe ""
        }
    }

    @Test
    fun `when consumption tab is selected and doCalculation is triggered but refueled is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.refueled = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when consumption tab is selected and doCalculation is triggered but kmTraveled is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.kmTraveled = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when consumption tab is selected and doCalculation is triggered but fuelPrice is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.fuelPrice = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when consumption tab is selected and doCalculation is triggered then ShowResultForConsumptionTab with right data should be sent`() =
        runTest {
            val expectedAvgConsumption = 10.0
            val expectedPrice = 75.0

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowResultForConsumptionTab(
                    avgConsumption = expectedAvgConsumption,
                    price = expectedPrice
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when costs tab is selected and doCalculation is triggered but avgFuelConsumption is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.COSTS
            systemUnderTest.avgFuelConsumption = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when costs tab is selected and doCalculation is triggered but kmTraveled is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.COSTS
            systemUnderTest.kmTraveled = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when costs tab is selected and doCalculation is triggered but fuelPrice is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.COSTS
            systemUnderTest.fuelPrice = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when costs tab is selected and doCalculation is triggered but numberOfPeople is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.COSTS
            systemUnderTest.numberOfPeople = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when costs tab is selected and doCalculation is triggered then ShowResultForConsumptionTab with right data should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.COSTS
            val expectedRequiredAmountOfFuel = 25.0
            val expectedCostForTravel = 187.5
            val expectedCostPerPerson = 46.875

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowResultForCostsTab(
                    requiredAmountOfFuel = expectedRequiredAmountOfFuel,
                    costForTravel = expectedCostForTravel,
                    costPerPerson = expectedCostPerPerson
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when range tab is selected and doCalculation is triggered but avgFuelConsumption is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.RANGE
            systemUnderTest.avgFuelConsumption = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when range tab is selected and doCalculation is triggered but paid is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.RANGE
            systemUnderTest.paid = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when range tab is selected and doCalculation is triggered but fuelPrice is missing then ShowMessageAboutMissingData should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.RANGE
            systemUnderTest.fuelPrice = ""

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when range tab is selected and doCalculation is triggered then ShowResultForConsumptionTab with right data should be sent`() =
        runTest {
            systemUnderTest.currentTabSelected = SelectedTab.RANGE
            val expectedAmountOfFilledWithFuel = 25.0
            val expectedRangeInKm = 250.0

            systemUnderTest.doCalculation()

            systemUnderTest.calculatorEvent.test {
                awaitItem() shouldBe CalculatorViewModel.CalculatorEvent.ShowResultForRangeTab(
                    amountOfFilledWithFuel = expectedAmountOfFilledWithFuel,
                    rangeInKm = expectedRangeInKm
                )
                cancelAndConsumeRemainingEvents()
            }
        }
}