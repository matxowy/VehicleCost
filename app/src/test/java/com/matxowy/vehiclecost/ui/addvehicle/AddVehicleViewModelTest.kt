package com.matxowy.vehiclecost.ui.addvehicle

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.ui.addvehicle.AddVehicleViewModel.Companion.MILEAGE_STATE_KEY
import com.matxowy.vehiclecost.ui.addvehicle.AddVehicleViewModel.Companion.NAME_STATE_KEY
import com.matxowy.vehiclecost.util.MainCoroutineRule
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddVehicleViewModelTest {
    private val mockkVehicleDao = mockk<VehicleDao> {
        coEvery { insert(any()) } returns Unit
    }
    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private var mockkSavedStateHandle = SavedStateHandle(mapOf(NAME_STATE_KEY to "name", MILEAGE_STATE_KEY to 0))

    private var systemUnderTest = AddVehicleViewModel(
        vehicleDao = mockkVehicleDao,
        state = mockkSavedStateHandle,
        coroutineDispatcher = testCoroutineDispatcher
    )

    @Test
    fun `when state is saved then fields should be filled up with values`() {
        systemUnderTest.vehicleName shouldBe "name"
        systemUnderTest.vehicleMileage shouldBe 0
    }

    @Test
    fun `when state for vehicle name is null then field should be blank`() {
        mockkSavedStateHandle[NAME_STATE_KEY] = null

        systemUnderTest = AddVehicleViewModel(
            vehicleDao = mockkVehicleDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher
        )

        systemUnderTest.vehicleName shouldBe ""
    }

    @Test
    fun `when state for vehicle mileage is null then field should be blank`() {
        mockkSavedStateHandle[MILEAGE_STATE_KEY] = null

        systemUnderTest = AddVehicleViewModel(
            vehicleDao = mockkVehicleDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher
        )

        systemUnderTest.vehicleMileage shouldBe DEFAULT_INT_VALUE
    }

    @Test
    fun `when vehicleName is blank while onAddVehicleButtonClick method is triggered then ShowInvalidDataMessage should be sent`() = runTest {
        systemUnderTest.vehicleName = ""

        systemUnderTest.onAddVehicleButtonClick()

        systemUnderTest.addVehicleEvent.test {
            awaitItem() shouldBe AddVehicleViewModel.AddVehicleEvent.ShowInvalidDataMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when vehicleMileage have default value while onAddVehicleButtonClick method is triggered then ShowInvalidDataMessage should be sent`() = runTest {
        systemUnderTest.vehicleMileage = DEFAULT_INT_VALUE

        systemUnderTest.onAddVehicleButtonClick()

        systemUnderTest.addVehicleEvent.test {
            awaitItem() shouldBe AddVehicleViewModel.AddVehicleEvent.ShowInvalidDataMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddVehicleButtonClick method is triggered and the insertion was successful then NavigateToStatisticsWithResult should be sent`() =
        runTest {
            systemUnderTest.onAddVehicleButtonClick()

            systemUnderTest.addVehicleEvent.test {
                awaitItem() shouldBe AddVehicleViewModel.AddVehicleEvent.NavigateToStatisticsWithResult(ADD_RESULT_OK)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onAddVehicleButtonClick method is triggered but inserting throws Exception then ShowAddErrorMessage should be sent`() = runTest {
        coEvery { mockkVehicleDao.insert(any()) } throws Exception()

        systemUnderTest.onAddVehicleButtonClick()

        systemUnderTest.addVehicleEvent.test {
            awaitItem() shouldBe AddVehicleViewModel.AddVehicleEvent.ShowAddErrorMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddVehicleButtonClick method is triggered then insert method with proper vehicle object is called`() = runTest {
        val vehicle = Vehicle(name = systemUnderTest.vehicleName, mileage = systemUnderTest.vehicleMileage.toString().toInt())

        systemUnderTest.onAddVehicleButtonClick()

        coVerify(exactly = 1) { mockkVehicleDao.insert(vehicle) }
    }
}