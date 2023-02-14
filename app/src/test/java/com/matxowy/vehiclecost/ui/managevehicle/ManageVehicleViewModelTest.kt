package com.matxowy.vehiclecost.ui.managevehicle

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.ui.managevehicle.ManageVehicleViewModel.Companion.MILEAGE_STATE_KEY
import com.matxowy.vehiclecost.ui.managevehicle.ManageVehicleViewModel.Companion.NAME_STATE_KEY
import com.matxowy.vehiclecost.util.MainCoroutineRule
import com.matxowy.vehiclecost.util.VehicleTestHelper.VEHICLE_ID
import com.matxowy.vehiclecost.util.VehicleTestHelper.VEHICLE_MILEAGE
import com.matxowy.vehiclecost.util.VehicleTestHelper.VEHICLE_NAME
import com.matxowy.vehiclecost.util.VehicleTestHelper.listOfVehicles
import com.matxowy.vehiclecost.util.VehicleTestHelper.vehicle
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_STRING_VALUE
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
import java.lang.Exception

@ExperimentalCoroutinesApi
class ManageVehicleViewModelTest {
    private val mockkVehicleDao = mockk<VehicleDao> {
        coEvery { getVehicles() } returns flowOf(listOfVehicles())
        coEvery { getVehicleById(any()) } returns vehicle()
        coEvery { delete(any()) } returns Unit
        coEvery { update(any()) } returns Unit
    }
    private val mockkLocalPreferences = mockk<LocalPreferencesApi> {
        every { getSelectedVehicleId() } returns VEHICLE_ID
    }
    private val mockkSavedStateHandle = SavedStateHandle(
        mapOf(
            NAME_STATE_KEY to VEHICLE_NAME,
            MILEAGE_STATE_KEY to VEHICLE_MILEAGE
        )
    )
    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private lateinit var systemUnderTest: ManageVehicleViewModel

    @Before
    fun setup() {
        systemUnderTest = ManageVehicleViewModel(
            vehicleDao = mockkVehicleDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )
    }

    @Test
    fun `when state is saved then fields should be filled with these values`() {
        systemUnderTest.apply {
            vehicleName shouldBe VEHICLE_NAME
            vehicleMileage shouldBe VEHICLE_MILEAGE
        }
    }

    @Test
    fun `when state is null then fields should be filled with default values`() {
        mockkSavedStateHandle[NAME_STATE_KEY] = null
        mockkSavedStateHandle[MILEAGE_STATE_KEY] = null

        systemUnderTest.apply {
            vehicleName shouldBe DEFAULT_STRING_VALUE
            vehicleMileage shouldBe DEFAULT_INT_VALUE
        }
    }

    @Test
    fun `when onSelectVehicle method is triggered and getting vehicle was successfully then fields should be set from this vehicle`() = runTest {
        systemUnderTest.onSelectVehicle(VEHICLE_ID)

        systemUnderTest.apply {
            vehicleId shouldBe vehicle().vehicleId
            vehicleName shouldBe vehicle().name
            vehicleMileage shouldBe vehicle().mileage
        }
    }

    @Test
    fun `when onSelectVehicle method is triggered and getting vehicle was successfully then SetFieldsWithData should be sent`() = runTest {
        systemUnderTest.onSelectVehicle(VEHICLE_ID)

        systemUnderTest.manageVehicleEvent.test {
            awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.SetFieldsWithData
        }
    }

    @Test
    fun `when onSelectVehicle method is triggered but getting vehicle throw exception then ShowDefaultErrorMessage should be sent`() = runTest {
        coEvery { mockkVehicleDao.getVehicleById(any()) } throws Exception()

        systemUnderTest.onSelectVehicle(VEHICLE_ID)

        systemUnderTest.manageVehicleEvent.test {
            awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowDefaultErrorMessage
        }
    }

    @Test
    fun `when onDeleteVehicleButtonClick method is triggered but current selected vehicle is same as get vehicle then ShowCannotDeleteCurrentSelectedVehicleMessage should be sent`() =
        runTest {
            systemUnderTest.onDeleteVehicleButtonClick(VEHICLE_ID)

            systemUnderTest.manageVehicleEvent.test {
                awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowCannotDeleteCurrentSelectedVehicleMessage
            }
        }

    @Test
    fun `when onDeleteVehicleButtonClick method is triggered but getting vehicle throws error then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkVehicleDao.getVehicleById(any()) } throws Exception()

            systemUnderTest.onDeleteVehicleButtonClick(VEHICLE_ID)

            systemUnderTest.manageVehicleEvent.test {
                awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowDefaultErrorMessage
            }
        }

    @Test
    fun `when onDeleteVehicleButtonClick method is triggered and current selected vehicle differ from picked vehicle id then ShowDeleteConfirmMessageWithUndoOption should be sent`() =
        runTest {
            coEvery { mockkLocalPreferences.getSelectedVehicleId() } returns 10

            systemUnderTest.onDeleteVehicleButtonClick(VEHICLE_ID)

            systemUnderTest.manageVehicleEvent.test {
                awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowDeleteConfirmMessageWithUndoOption(vehicle())
            }
        }

    @Test
    fun `when onDeleteVehicleButtonClick method is triggered and current selected vehicle differ from picked vehicle id then delete should be called`() =
        runTest {
            coEvery { mockkLocalPreferences.getSelectedVehicleId() } returns 10

            systemUnderTest.onDeleteVehicleButtonClick(VEHICLE_ID)

            coVerify(exactly = 1) { mockkVehicleDao.delete(vehicle()) }
        }

    @Test
    fun `when onDeleteVehicleButtonClick method is triggered and current selected vehicle differ from picked vehicle id but deleting throws error then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkLocalPreferences.getSelectedVehicleId() } returns 10
            coEvery { mockkVehicleDao.delete(any()) } throws Exception()

            systemUnderTest.onDeleteVehicleButtonClick(VEHICLE_ID)

            systemUnderTest.manageVehicleEvent.test {
                awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowDefaultErrorMessage
            }
        }

    @Test
    fun `when onUndoDeleteVehicleClick method is triggered then insert should be called`() = runTest {
        systemUnderTest.onUndoDeleteVehicleClick(vehicle())

        coVerify(exactly = 1) { mockkVehicleDao.insert(vehicle()) }
    }

    @Test
    fun `when onUndoDeleteVehicleClick method is triggered but insert throws exception then ShowDefaultErrorMessage should be sent`() = runTest {
        coEvery { mockkVehicleDao.insert(any()) } throws Exception()

        systemUnderTest.onUndoDeleteVehicleClick(vehicle())

        systemUnderTest.manageVehicleEvent.test {
            awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowDefaultErrorMessage
        }
    }

    @Test
    fun `when onEditVehicleButtonClick method is triggered then update should be called with proper vehicle`() = runTest {
        systemUnderTest.vehicleName = "edited name"
        val vehicle = Vehicle(vehicleId = systemUnderTest.vehicleId, name = systemUnderTest.vehicleName, mileage = systemUnderTest.vehicleMileage)

        systemUnderTest.onEditVehicleButtonClick()

        coVerify(exactly = 1) { mockkVehicleDao.update(vehicle) }
    }

    @Test
    fun `when onEditVehicleButtonClick method is triggered and updating was successfully then ShowUpdateConfirmMessage should be sent`() = runTest {
        systemUnderTest.vehicleName = "edited name"

        systemUnderTest.onEditVehicleButtonClick()

        systemUnderTest.manageVehicleEvent.test {
            awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowUpdateConfirmMessage
        }
    }

    @Test
    fun `when onEditVehicleButtonClick method is triggered but updating throws exception then ShowEditingErrorMessage should be sent`() = runTest {
        systemUnderTest.vehicleName = "edited name"
        coEvery { mockkVehicleDao.update(any()) } throws Exception()

        systemUnderTest.onEditVehicleButtonClick()

        systemUnderTest.manageVehicleEvent.test {
            awaitItem() shouldBe ManageVehicleViewModel.ManageVehicleEvents.ShowEditingErrorMessage
        }
    }
}