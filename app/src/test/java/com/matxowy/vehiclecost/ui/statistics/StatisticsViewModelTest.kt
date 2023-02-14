package com.matxowy.vehiclecost.ui.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.MainCoroutineRule
import com.matxowy.vehiclecost.util.VehicleTestHelper.VEHICLE_ID
import com.matxowy.vehiclecost.util.VehicleTestHelper.listOfVehicles
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import io.kotest.matchers.collections.shouldStartWith
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StatisticsViewModelTest {
    private val mockkVehicleDao = mockk<VehicleDao> {
        coEvery { getVehicles() } returns flowOf(listOfVehicles())
    }
    private val mockkRefuelDao = mockk<RefuelDao> {
        coEvery { getSumOfCosts(any()) } returns SUM_OF_REFUEL_COSTS
        coEvery { getSumOfRefuels(any()) } returns SUM_OF_REFUELS
        coEvery { getLastPriceOfFuel(any()) } returns LAST_PRICE_OF_FUEL
    }
    private val mockkRepairDao = mockk<RepairDao> {
        coEvery { getSumCostOfRepair(any()) } returns SUM_OF_REPAIR_COSTS
        coEvery { getMaxCostOfRepair(any()) } returns MAX_COST_OF_REPAIR
        coEvery { getLastCost(any()) } returns LAST_COST_OF_REPAIR
    }
    private val mockkLocalPreferences = mockk<LocalPreferencesApi> {
        every { getSelectedVehicleId() } returns VEHICLE_ID
        every { getSelectedVehiclePosition() } returns 1
        every { saveSelectedVehicleId(any()) } returns Unit
        every { saveSelectedVehiclePosition(any()) } returns Unit
    }

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private lateinit var systemUnderTest: StatisticsViewModel

    @Before
    fun setup() {
        systemUnderTest = StatisticsViewModel(
            refuelDao = mockkRefuelDao,
            repairDao = mockkRepairDao,
            vehicleDao = mockkVehicleDao,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )
    }

    companion object {
        const val SUM_OF_REFUEL_COSTS = 500.0
        const val SUM_OF_REFUELS = 200.0
        const val LAST_PRICE_OF_FUEL = 7.5
        const val SUM_OF_REPAIR_COSTS = 800.0
        const val MAX_COST_OF_REPAIR = 550.0
        const val LAST_COST_OF_REPAIR = 250.0
    }

    @Test
    fun `when initiate view model then local preferences and daos calls should be called`() {
        coVerify(exactly = 1) { mockkLocalPreferences.getSelectedVehicleId() }
        coVerify(exactly = 1) { mockkRefuelDao.getSumOfCosts(VEHICLE_ID) }
        coVerify(exactly = 1) { mockkRefuelDao.getSumOfRefuels(VEHICLE_ID) }
        coVerify(exactly = 1) { mockkRefuelDao.getLastPriceOfFuel(VEHICLE_ID) }
        coVerify(exactly = 1) { mockkRepairDao.getSumCostOfRepair(VEHICLE_ID) }
        coVerify(exactly = 1) { mockkRepairDao.getMaxCostOfRepair(VEHICLE_ID) }
        coVerify(exactly = 1) { mockkRepairDao.getLastCost(VEHICLE_ID) }
    }

    @Test
    fun `after initiate view model fields should be set up with proper values`() {
        systemUnderTest.sumCostsOfRefuels.observeForever { it shouldBe SUM_OF_REFUEL_COSTS }
        systemUnderTest.sumOfFuelAmount.observeForever { it shouldBe SUM_OF_REFUELS }
        systemUnderTest.lastPriceOfFuel.observeForever { it shouldBe LAST_PRICE_OF_FUEL }
        systemUnderTest.sumCostOfRepair.observeForever { it shouldBe SUM_OF_REPAIR_COSTS }
        systemUnderTest.maxCostOfRepair.observeForever { it shouldBe MAX_COST_OF_REPAIR }
        systemUnderTest.lastCostOfRepair.observeForever { it shouldBe LAST_COST_OF_REPAIR }
    }

    @Test
    fun `when getSelectedVehiclePosition method is triggered then getSelectedVehiclePosition from local preferences should be called`() = runTest {
        systemUnderTest.getSelectedVehiclePosition()

        coVerify(exactly = 1) { mockkLocalPreferences.getSelectedVehiclePosition() }
    }

    @Test
    fun `when saveSelectedVehicleAndRefreshStatistics method is triggered with position other than 0 then local preferences and daos calls should be called`() =
        runTest {
            systemUnderTest.saveSelectedVehicleAndRefreshStatistics(1, VEHICLE_ID)

            coVerify(exactly = 1) { mockkLocalPreferences.saveSelectedVehicleId(VEHICLE_ID) }
            coVerify(exactly = 1) { mockkLocalPreferences.saveSelectedVehiclePosition(1) }
            coVerify(exactly = 2) { mockkRefuelDao.getSumOfCosts(VEHICLE_ID) }
            coVerify(exactly = 2) { mockkRefuelDao.getSumOfRefuels(VEHICLE_ID) }
            coVerify(exactly = 2) { mockkRefuelDao.getLastPriceOfFuel(VEHICLE_ID) }
            coVerify(exactly = 2) { mockkRepairDao.getSumCostOfRepair(VEHICLE_ID) }
            coVerify(exactly = 2) { mockkRepairDao.getMaxCostOfRepair(VEHICLE_ID) }
            coVerify(exactly = 2) { mockkRepairDao.getLastCost(VEHICLE_ID) }
        }

    @Test
    fun `when saveSelectedVehicleAndRefreshStatistics method is triggered with position equals 0 then local preferences and daos calls should not be called`() =
        runTest {
            systemUnderTest.saveSelectedVehicleAndRefreshStatistics(0, VEHICLE_ID)

            coVerify(exactly = 0) { mockkLocalPreferences.saveSelectedVehicleId(VEHICLE_ID) }
            coVerify(exactly = 0) { mockkLocalPreferences.saveSelectedVehiclePosition(1) }
            coVerify(exactly = 1) { mockkRefuelDao.getSumOfCosts(VEHICLE_ID) }
            coVerify(exactly = 1) { mockkRefuelDao.getSumOfRefuels(VEHICLE_ID) }
            coVerify(exactly = 1) { mockkRefuelDao.getLastPriceOfFuel(VEHICLE_ID) }
            coVerify(exactly = 1) { mockkRepairDao.getSumCostOfRepair(VEHICLE_ID) }
            coVerify(exactly = 1) { mockkRepairDao.getMaxCostOfRepair(VEHICLE_ID) }
            coVerify(exactly = 1) { mockkRepairDao.getLastCost(VEHICLE_ID) }
        }

    @Test
    fun `when onAddVehicleResult method is triggered with ADD_RESULT_OK then ShowVehicleSavedConfirmationMessage should be sent`() = runTest {
        systemUnderTest.onAddVehicleResult(ADD_RESULT_OK)

        systemUnderTest.statisticsEvent.test {
            awaitItem() shouldBe StatisticsViewModel.StatisticsEvent.ShowVehicleSavedConfirmationMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddNewRefuelClick method is triggered then NavigateToAddRefuelScreen should be sent`() = runTest {
        systemUnderTest.onAddNewRefuelClick()

        systemUnderTest.statisticsEvent.test {
            awaitItem() shouldBe StatisticsViewModel.StatisticsEvent.NavigateToAddRefuelScreen
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddNewRepairClick method is triggered then NavigateToAddRepairScreen should be sent`() = runTest {
        systemUnderTest.onAddNewRepairClick()

        systemUnderTest.statisticsEvent.test {
            awaitItem() shouldBe StatisticsViewModel.StatisticsEvent.NavigateToAddRepairScreen
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddNewVehicleClick method is triggered then NavigateToAddVehicleScreen should be sent`() = runTest {
        systemUnderTest.onAddNewVehicleClick()

        systemUnderTest.statisticsEvent.test {
            awaitItem() shouldBe StatisticsViewModel.StatisticsEvent.NavigateToAddVehicleScreen
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when getListOfVehiclesNamesWithAddingOptionOnFirstPosition method is triggered then new list with adding option on first position should be return`() =
        runTest {
            val listOfVehiclesNames = listOfVehicles()
            val addNewVehicleText = "Add new car"

            val result = systemUnderTest.getListOfVehiclesNamesWithAddingOptionOnFirstPosition(listOfVehiclesNames, addNewVehicleText)

            result shouldStartWith Vehicle(name = addNewVehicleText)
        }

    @Test
    fun `when getListOfVehiclesNamesWithAddingOptionOnFirstPosition method is triggered then proper new list should be return`() = runTest {
        val listOfVehiclesNames = listOfVehicles()
        val addNewVehicleText = "Add new car"
        val expectedList = listOfVehiclesNames.toMutableList()
        expectedList.add(0, Vehicle(name = addNewVehicleText))

        val result = systemUnderTest.getListOfVehiclesNamesWithAddingOptionOnFirstPosition(listOfVehiclesNames, addNewVehicleText)

        result shouldBe expectedList
    }
}