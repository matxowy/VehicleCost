package com.matxowy.vehiclecost.ui.addeditrefuel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.AMOUNT_OF_FUEL_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.COMMENTS_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.COST_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.DATE_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.FUEL_TYPE_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.FULL_REFUELED_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.MILEAGE_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.PRICE_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.REFUEL_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelViewModel.Companion.TIME_STATE_KEY
import com.matxowy.vehiclecost.util.MainCoroutineRule
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.*

@ExperimentalCoroutinesApi
class AddEditRefuelViewModelTest {

    private val mockkRefuelDao = mockk<RefuelDao> {
        coEvery { update(any()) } returns Unit
        coEvery { insert(any()) } returns Unit
    }
    private val mockkVehicleDao = mockk<VehicleDao> {
        every { getVehicleMileageById(any()) } returns flowOf(1234)
        every { updateMileageOfVehicle(any(), any()) } returns Unit
    }
    private val mockkLocalPreferences = mockk<LocalPreferencesApi> {
        every { getSelectedVehicleId() } returns VEHICLE_ID
    }
    private val testRefuel = Refuel(
        mileage = MILEAGE,
        date = DATE,
        time = TIME,
        amountOfFuel = AMOUNT_OF_FUEL,
        cost = COST,
        price = PRICE,
        fuelType = FUEL_TYPE,
        fullRefueled = FULL_REFUELED,
        comments = COMMENTS,
        vehicleId = VEHICLE_ID,
    )
    private var mockkSavedStateHandle = SavedStateHandle(
        mapOf(
            MILEAGE_STATE_KEY to MILEAGE,
            DATE_STATE_KEY to DATE,
            TIME_STATE_KEY to TIME,
            AMOUNT_OF_FUEL_STATE_KEY to AMOUNT_OF_FUEL,
            COST_STATE_KEY to COST,
            PRICE_STATE_KEY to PRICE,
            FULL_REFUELED_STATE_KEY to FULL_REFUELED,
            COMMENTS_STATE_KEY to COMMENTS,
            FUEL_TYPE_STATE_KEY to FUEL_TYPE,
        )
    )

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private var systemUnderTest = AddEditRefuelViewModel(
        refuelDao = mockkRefuelDao,
        vehicleDao = mockkVehicleDao,
        state = mockkSavedStateHandle,
        coroutineDispatcher = testCoroutineDispatcher,
        localPreferences = mockkLocalPreferences,
    )

    companion object {
        const val VEHICLE_ID = 0
        const val MILEAGE = 100
        const val DATE = "11.11.2011"
        const val TIME = "12:54"
        const val AMOUNT_OF_FUEL = 42.23
        const val COST = 190.03
        const val PRICE = 4.5
        const val FULL_REFUELED = true
        const val COMMENTS = "Comment"
        const val FUEL_TYPE = "ON"
        const val LOCAL_DATE_FORMATTED_STRING = "2012-12-12"
        const val LOCAL_DATE_TIME_FORMATTED_STRING = "13:53"
        val LOCAL_DATE_TIME: Clock = Clock.fixed(Instant.parse("2012-12-12T13:53:30.00Z"), ZoneId.of("UTC"))
    }

    @Test
    fun `when state is saved then fields should be filled with these values`() {
        systemUnderTest.apply {
            mileage shouldBe MILEAGE
            date shouldBe DATE
            time shouldBe TIME
            amountOfFuel shouldBe AMOUNT_OF_FUEL
            cost shouldBe COST
            price shouldBe PRICE
            fullRefueled shouldBe FULL_REFUELED
            comments shouldBe COMMENTS
            fuelType shouldBe FUEL_TYPE
        }
    }

    @Test
    fun `when states are null but refuel is not null then fields should have values from refuel`() {
        mockkSavedStateHandle[REFUEL_STATE_KEY] = testRefuel
        mockkSavedStateHandle[MILEAGE_STATE_KEY] = null
        mockkSavedStateHandle[DATE_STATE_KEY] = null
        mockkSavedStateHandle[TIME_STATE_KEY] = null
        mockkSavedStateHandle[AMOUNT_OF_FUEL_STATE_KEY] = null
        mockkSavedStateHandle[COST_STATE_KEY] = null
        mockkSavedStateHandle[PRICE_STATE_KEY] = null
        mockkSavedStateHandle[FULL_REFUELED_STATE_KEY] = null
        mockkSavedStateHandle[COMMENTS_STATE_KEY] = null
        mockkSavedStateHandle[FUEL_TYPE_STATE_KEY] = null

        systemUnderTest = AddEditRefuelViewModel(
            refuelDao = mockkRefuelDao,
            vehicleDao = mockkVehicleDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )

        systemUnderTest.apply {
            mileage shouldBe testRefuel.mileage
            date shouldBe testRefuel.date
            time shouldBe testRefuel.time
            amountOfFuel shouldBe testRefuel.amountOfFuel
            cost shouldBe testRefuel.cost
            price shouldBe testRefuel.price
            fullRefueled shouldBe testRefuel.fullRefueled
            comments shouldBe testRefuel.comments
            fuelType shouldBe testRefuel.fuelType
        }
    }

    @Test
    fun `when states and refuel are null then field should have default value`() {
        mockkSavedStateHandle[MILEAGE_STATE_KEY] = null
        mockkSavedStateHandle[DATE_STATE_KEY] = null
        mockkSavedStateHandle[TIME_STATE_KEY] = null
        mockkSavedStateHandle[AMOUNT_OF_FUEL_STATE_KEY] = null
        mockkSavedStateHandle[COST_STATE_KEY] = null
        mockkSavedStateHandle[PRICE_STATE_KEY] = null
        mockkSavedStateHandle[FULL_REFUELED_STATE_KEY] = null
        mockkSavedStateHandle[COMMENTS_STATE_KEY] = null
        mockkSavedStateHandle[FUEL_TYPE_STATE_KEY] = null
        val localDateTime = LocalDateTime.now(LOCAL_DATE_TIME)
        val localDate = LocalDate.now(LOCAL_DATE_TIME)
        mockkStatic(LocalDate::class)
        mockkStatic(LocalDateTime::class)
        every { LocalDate.now() } returns localDate
        every { LocalDateTime.now() } returns localDateTime

        systemUnderTest = AddEditRefuelViewModel(
            refuelDao = mockkRefuelDao,
            vehicleDao = mockkVehicleDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )

        systemUnderTest.apply {
            mileage shouldBe ""
            date shouldBe LOCAL_DATE_FORMATTED_STRING
            time shouldBe LOCAL_DATE_TIME_FORMATTED_STRING
            amountOfFuel shouldBe ""
            cost shouldBe ""
            price shouldBe ""
            fullRefueled shouldBe false
            comments shouldBe ""
            fuelType shouldBe FUEL_TYPE
        }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered and mileage is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() = runTest {
        systemUnderTest.mileage = ""

        systemUnderTest.onSaveRefueledClick()

        systemUnderTest.addEditRefuelEvent.test {
            awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowFieldsCannotBeEmptyMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered and amountOfFuel is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() =
        runTest {
            systemUnderTest.amountOfFuel = ""

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowFieldsCannotBeEmptyMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRefueledClick method is triggered and cost is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() = runTest {
        systemUnderTest.cost = ""

        systemUnderTest.onSaveRefueledClick()

        systemUnderTest.addEditRefuelEvent.test {
            awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowFieldsCannotBeEmptyMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered and price is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() = runTest {
        systemUnderTest.price = ""

        systemUnderTest.onSaveRefueledClick()

        systemUnderTest.addEditRefuelEvent.test {
            awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowFieldsCannotBeEmptyMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered and mileage is less than last mileage then ShowMileageCannotBeLessThanPreviousMessage event should be sent`() =
        runTest {
            systemUnderTest = AddEditRefuelViewModel(
                refuelDao = mockkRefuelDao,
                vehicleDao = mockkVehicleDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )
            systemUnderTest.mileage = 111
            systemUnderTest.lastMileage.observeForever {}

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowMileageCannotBeLessThanPreviousMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRefueledClick method is triggered and refuel is not null then NavigateToHistoryWithResult with edit code event should be sent`() =
        runTest {
            mockkSavedStateHandle[REFUEL_STATE_KEY] = testRefuel
            systemUnderTest = AddEditRefuelViewModel(
                refuelDao = mockkRefuelDao,
                vehicleDao = mockkVehicleDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.NavigateToHistoryWithResult(EDIT_RESULT_OK)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRefueledClick method is triggered and refuel is not null then updateMileageOfVehicle and update should be called`() = runTest {
        mockkSavedStateHandle[REFUEL_STATE_KEY] = testRefuel
        systemUnderTest = AddEditRefuelViewModel(
            refuelDao = mockkRefuelDao,
            vehicleDao = mockkVehicleDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )

        systemUnderTest.onSaveRefueledClick()

        coVerify(exactly = 1) { mockkRefuelDao.update(testRefuel) }
        coVerify(exactly = 1) { mockkVehicleDao.updateMileageOfVehicle(vehicleId = testRefuel.vehicleId, mileage = testRefuel.mileage) }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered, refuel is not null and updateMileageOfVehicle throws exception then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkVehicleDao.updateMileageOfVehicle(any(), any()) } throws Exception()
            mockkSavedStateHandle[REFUEL_STATE_KEY] = testRefuel
            systemUnderTest = AddEditRefuelViewModel(
                refuelDao = mockkRefuelDao,
                vehicleDao = mockkVehicleDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowDefaultErrorMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRefueledClick method is triggered, refuel is not null and refuel update throws exception then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkRefuelDao.update(any()) } throws Exception()
            mockkSavedStateHandle[REFUEL_STATE_KEY] = testRefuel
            systemUnderTest = AddEditRefuelViewModel(
                refuelDao = mockkRefuelDao,
                vehicleDao = mockkVehicleDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowDefaultErrorMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRefueledClick method is triggered and refuel is null then updateMileageOfVehicle and insert should be called`() = runTest {
        val createdRefuel = Refuel(
            mileage = systemUnderTest.mileage.toString().toInt(),
            date = systemUnderTest.date!!,
            time = systemUnderTest.time!!,
            amountOfFuel = systemUnderTest.amountOfFuel.toString().toDouble(),
            cost = systemUnderTest.cost.toString().toDouble(),
            price = systemUnderTest.price.toString().toDouble(),
            fuelType = systemUnderTest.fuelType,
            fullRefueled = systemUnderTest.fullRefueled,
            comments = systemUnderTest.comments,
            vehicleId = VEHICLE_ID
        )

        systemUnderTest.onSaveRefueledClick()

        coVerify { mockkVehicleDao.updateMileageOfVehicle(createdRefuel.vehicleId, createdRefuel.mileage) }
        coVerify { mockkRefuelDao.insert(createdRefuel) }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered and refuel is null then NavigateToHistoryWithResult with add code should be sent`() = runTest {
        systemUnderTest.onSaveRefueledClick()

        systemUnderTest.addEditRefuelEvent.test {
            awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.NavigateToHistoryWithResult(ADD_RESULT_OK)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRefueledClick method is triggered, refuel is null and updateMileageOfVehicle throws exception then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkVehicleDao.updateMileageOfVehicle(any(), any()) } throws Exception()
            systemUnderTest = AddEditRefuelViewModel(
                refuelDao = mockkRefuelDao,
                vehicleDao = mockkVehicleDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowDefaultErrorMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRefueledClick method is triggered, refuel is null and refuel update throws exception then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkRefuelDao.insert(any()) } throws Exception()
            systemUnderTest = AddEditRefuelViewModel(
                refuelDao = mockkRefuelDao,
                vehicleDao = mockkVehicleDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRefueledClick()

            systemUnderTest.addEditRefuelEvent.test {
                awaitItem() shouldBe AddEditRefuelViewModel.AddEditRefuelEvent.ShowDefaultErrorMessage
                cancelAndConsumeRemainingEvents()
            }
        }
}