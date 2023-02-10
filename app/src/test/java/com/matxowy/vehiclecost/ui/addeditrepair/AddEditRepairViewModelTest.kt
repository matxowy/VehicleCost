package com.matxowy.vehiclecost.ui.addeditrepair

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.COMMENTS_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.COST_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.DATE_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.MILEAGE_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.REPAIR_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.TIME_STATE_KEY
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairViewModel.Companion.TITLE_STATE_KEY
import com.matxowy.vehiclecost.util.MainCoroutineRule
import com.matxowy.vehiclecost.util.constants.ResultCodes
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.*

@ExperimentalCoroutinesApi
class AddEditRepairViewModelTest {
    private val mockkRepairDao = mockk<RepairDao> {
        coEvery { update(any()) } returns Unit
        coEvery { insert(any()) } returns Unit
    }
    private val mockkLocalPreferences = mockk<LocalPreferencesApi> {
        every { getSelectedVehicleId() } returns VEHICLE_ID
    }
    private val testRepair = Repair(
        title = TITLE,
        mileage = MILEAGE,
        cost = COST,
        date = DATE,
        time = TIME,
        comments = COMMENTS,
        vehicleId = 0,
    )
    private var mockkSavedStateHandle = SavedStateHandle(
        mapOf(
            TITLE_STATE_KEY to TITLE,
            MILEAGE_STATE_KEY to MILEAGE,
            COST_STATE_KEY to COST,
            DATE_STATE_KEY to DATE,
            TIME_STATE_KEY to TIME,
            COMMENTS_STATE_KEY to COMMENTS,
        )
    )

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private var systemUnderTest = AddEditRepairViewModel(
        repairDao = mockkRepairDao,
        state = mockkSavedStateHandle,
        coroutineDispatcher = testCoroutineDispatcher,
        localPreferences = mockkLocalPreferences,
    )

    companion object {
        const val VEHICLE_ID = 0
        const val TITLE = "Repair"
        const val MILEAGE = 100
        const val DATE = "11.11.2011"
        const val TIME = "12:54"
        const val COST = 190.03
        const val COMMENTS = "Comment"
        const val LOCAL_DATE_FORMATTED_STRING = "2012-12-12"
        const val LOCAL_DATE_TIME_FORMATTED_STRING = "13:53"
        val LOCAL_DATE_TIME: Clock = Clock.fixed(Instant.parse("2012-12-12T13:53:30.00Z"), ZoneId.of("UTC"))
    }

    @Test
    fun `when state is saved then fields should be filled with these values`() {
        systemUnderTest.apply {
            title shouldBe TITLE
            mileage shouldBe MILEAGE
            date shouldBe DATE
            time shouldBe TIME
            cost shouldBe COST
            comments shouldBe COMMENTS
        }
    }

    @Test
    fun `when states are null but refuel is not null then fields should have values from repair`() {
        mockkSavedStateHandle[REPAIR_STATE_KEY] = testRepair
        mockkSavedStateHandle[TITLE_STATE_KEY] = null
        mockkSavedStateHandle[MILEAGE_STATE_KEY] = null
        mockkSavedStateHandle[DATE_STATE_KEY] = null
        mockkSavedStateHandle[TIME_STATE_KEY] = null
        mockkSavedStateHandle[COST_STATE_KEY] = null
        mockkSavedStateHandle[COMMENTS_STATE_KEY] = null

        systemUnderTest = AddEditRepairViewModel(
            repairDao = mockkRepairDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )

        systemUnderTest.apply {
            title shouldBe testRepair.title
            mileage shouldBe testRepair.mileage
            date shouldBe testRepair.date
            time shouldBe testRepair.time
            cost shouldBe testRepair.cost
            comments shouldBe testRepair.comments
        }
    }

    @Test
    fun `when states and repair are null then field should have default value`() {
        mockkSavedStateHandle[TITLE_STATE_KEY] = null
        mockkSavedStateHandle[MILEAGE_STATE_KEY] = null
        mockkSavedStateHandle[DATE_STATE_KEY] = null
        mockkSavedStateHandle[TIME_STATE_KEY] = null
        mockkSavedStateHandle[COST_STATE_KEY] = null
        mockkSavedStateHandle[COMMENTS_STATE_KEY] = null
        val localDateTime = LocalDateTime.now(LOCAL_DATE_TIME)
        val localDate = LocalDate.now(LOCAL_DATE_TIME)
        mockkStatic(LocalDate::class)
        mockkStatic(LocalDateTime::class)
        every { LocalDate.now() } returns localDate
        every { LocalDateTime.now() } returns localDateTime

        systemUnderTest = AddEditRepairViewModel(
            repairDao = mockkRepairDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )

        systemUnderTest.apply {
            title shouldBe ""
            mileage shouldBe ""
            date shouldBe LOCAL_DATE_FORMATTED_STRING
            time shouldBe LOCAL_DATE_TIME_FORMATTED_STRING
            cost shouldBe ""
            comments shouldBe ""
        }
    }

    @Test
    fun `when onSaveRepairClick method is triggered and mileage is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() = runTest {
        systemUnderTest.mileage = ""

        systemUnderTest.onSaveRepairClick()

        systemUnderTest.addEditRepairEvent.test {
            awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.ShowFieldsCannotBeEmptyMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRepairClick method is triggered and title is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() =
        runTest {
            systemUnderTest.title = ""

            systemUnderTest.onSaveRepairClick()

            systemUnderTest.addEditRepairEvent.test {
                awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.ShowFieldsCannotBeEmptyMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRepairClick method is triggered and cost is blank then ShowFieldsCannotBeEmptyMessage event should be sent`() = runTest {
        systemUnderTest.cost = ""

        systemUnderTest.onSaveRepairClick()

        systemUnderTest.addEditRepairEvent.test {
            awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.ShowFieldsCannotBeEmptyMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRepairClick method is triggered and repair is not null then NavigateToHistoryWithResult with edit code event should be sent`() =
        runTest {
            mockkSavedStateHandle[REPAIR_STATE_KEY] = testRepair
            systemUnderTest = AddEditRepairViewModel(
                repairDao = mockkRepairDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRepairClick()

            systemUnderTest.addEditRepairEvent.test {
                awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.NavigateToHistoryWithResult(ResultCodes.EDIT_RESULT_OK)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRepairClick method is triggered and repair is not null then update should be called`() = runTest {
        mockkSavedStateHandle[REPAIR_STATE_KEY] = testRepair
        systemUnderTest = AddEditRepairViewModel(
            repairDao = mockkRepairDao,
            state = mockkSavedStateHandle,
            coroutineDispatcher = testCoroutineDispatcher,
            localPreferences = mockkLocalPreferences,
        )

        systemUnderTest.onSaveRepairClick()

        coVerify(exactly = 1) { mockkRepairDao.update(testRepair) }
    }

    @Test
    fun `when onSaveRepairClick method is triggered, repair is not null and repair update throws exception then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkRepairDao.update(any()) } throws Exception()
            mockkSavedStateHandle[REPAIR_STATE_KEY] = testRepair
            systemUnderTest = AddEditRepairViewModel(
                repairDao = mockkRepairDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRepairClick()

            systemUnderTest.addEditRepairEvent.test {
                awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.ShowDefaultErrorMessage
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when onSaveRepairClick method is triggered and repair is null then insert should be called`() = runTest {
        val createdRepair = Repair(
            title = systemUnderTest.title,
            mileage = systemUnderTest.mileage.toString().toInt(),
            date = systemUnderTest.date!!,
            time = systemUnderTest.time!!,
            cost = systemUnderTest.cost.toString().toDouble(),
            comments = systemUnderTest.comments,
            vehicleId = VEHICLE_ID
        )

        systemUnderTest.onSaveRepairClick()

        coVerify { mockkRepairDao.insert(createdRepair) }
    }

    @Test
    fun `when onSaveRepairClick method is triggered and repair is null then NavigateToHistoryWithResult with add code should be sent`() = runTest {
        systemUnderTest.onSaveRepairClick()

        systemUnderTest.addEditRepairEvent.test {
            awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.NavigateToHistoryWithResult(ResultCodes.ADD_RESULT_OK)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onSaveRepairClick method is triggered, repair is null and repair update throws exception then ShowDefaultErrorMessage should be sent`() =
        runTest {
            coEvery { mockkRepairDao.insert(any()) } throws Exception()
            systemUnderTest = AddEditRepairViewModel(
                repairDao = mockkRepairDao,
                state = mockkSavedStateHandle,
                coroutineDispatcher = testCoroutineDispatcher,
                localPreferences = mockkLocalPreferences,
            )

            systemUnderTest.onSaveRepairClick()

            systemUnderTest.addEditRepairEvent.test {
                awaitItem() shouldBe AddEditRepairViewModel.AddEditRepairEvent.ShowDefaultErrorMessage
                cancelAndConsumeRemainingEvents()
            }
        }
}