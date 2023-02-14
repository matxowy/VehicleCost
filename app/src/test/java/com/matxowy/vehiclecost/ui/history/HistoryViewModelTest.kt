package com.matxowy.vehiclecost.ui.history

import app.cash.turbine.test
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.util.MainCoroutineRule
import com.matxowy.vehiclecost.util.RefuelTestHelper
import com.matxowy.vehiclecost.util.RefuelTestHelper.VEHICLE_ID
import com.matxowy.vehiclecost.util.RepairTestHelper
import com.matxowy.vehiclecost.util.constants.ResultCodes.ADD_RESULT_OK
import com.matxowy.vehiclecost.util.constants.ResultCodes.EDIT_RESULT_OK
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HistoryViewModelTest {
    private val mockkRefuelDao = mockk<RefuelDao> {
        coEvery { insert(any()) } returns Unit
        coEvery { delete(any()) } returns Unit
        coEvery { getRefuels(any()) } returns flowOf(RefuelTestHelper.listOfRefuels())
    }
    private val mockkRepairDao = mockk<RepairDao> {
        coEvery { insert(any()) } returns Unit
        coEvery { delete(any()) } returns Unit
        coEvery { getRepairs(any()) } returns flowOf(RepairTestHelper.listOfRepairs())
    }
    private val mockkLocalPreferences = mockk<LocalPreferencesApi> {
        every { getSelectedVehicleId() } returns VEHICLE_ID
    }
    private val testRefuel = RefuelTestHelper.refuel()
    private val testRepair = RepairTestHelper.repair()

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testCoroutineDispatcher)

    private val systemUnderTest = HistoryViewModel(
        refuelDao = mockkRefuelDao,
        repairDao = mockkRepairDao,
        coroutineDispatcher = testCoroutineDispatcher,
        localPreferences = mockkLocalPreferences,
    )

    @Test
    fun `when onRefuelSwipe method is called and delete refuel was successfully then ShowUndoDeleteRefuelMessage should be sent`() = runTest {
        systemUnderTest.onRefuelSwiped(testRefuel)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowUndoDeleteRefuelMessage(testRefuel)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onRefuelSwipe method is called but delete refuel throw exception then ShowDefaultErrorMessage should be sent`() = runTest {
        coEvery { mockkRefuelDao.delete(testRefuel) } throws Exception()

        systemUnderTest.onRefuelSwiped(testRefuel)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowDefaultErrorMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onRepairSwipe method is called and delete refuel was successfully then ShowUndoDeleteRepairMessage should be sent`() = runTest {
        systemUnderTest.onRepairSwiped(testRepair)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowUndoDeleteRepairMessage(testRepair)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onRepairSwipe method is called but delete refuel throw exception then ShowDefaultErrorMessage should be sent`() = runTest {
        coEvery { mockkRepairDao.delete(testRepair) } throws Exception()

        systemUnderTest.onRepairSwiped(testRepair)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowDefaultErrorMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onUndoDeleteRefuelClick method is called then insert should be called`() = runTest {
        systemUnderTest.onUndoDeleteRefuelClick(testRefuel)

        coVerify(exactly = 1) { mockkRefuelDao.insert(testRefuel) }
    }

    @Test
    fun `when onUndoDeleteRepairClick method is called then insert should be called`() = runTest {
        systemUnderTest.onUndoDeleteRepairClick(testRepair)

        coVerify(exactly = 1) { mockkRepairDao.insert(testRepair) }
    }

    @Test
    fun `when onUndoDeleteRefuelClick method is called but insert refuel throw exception then ShowDefaultErrorMessage should be sent`() = runTest {
        coEvery { mockkRefuelDao.insert(testRefuel) } throws Exception()

        systemUnderTest.onUndoDeleteRefuelClick(testRefuel)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowDefaultErrorMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onUndoDeleteRepairClick method is called but insert refuel throw exception then ShowDefaultErrorMessage should be sent`() = runTest {
        coEvery { mockkRepairDao.insert(testRepair) } throws Exception()

        systemUnderTest.onUndoDeleteRepairClick(testRepair)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowDefaultErrorMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddEditRefuelResult method is called with ADD_RESULT_OK then ShowRefuelSavedConfirmationMessage should be sent`() = runTest {
        systemUnderTest.onAddEditRefuelResult(ADD_RESULT_OK)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowRefuelSavedConfirmationMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddEditRefuelResult method is called with EDIT_RESULT_OK then ShowRefuelSavedConfirmationMessage should be sent`() = runTest {
        systemUnderTest.onAddEditRefuelResult(EDIT_RESULT_OK)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowRefuelEditedConfirmationMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddEditRepairResult method is called with ADD_RESULT_OK then ShowRepairSavedConfirmationMessage should be sent`() = runTest {
        systemUnderTest.onAddEditRepairResult(ADD_RESULT_OK)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowRepairSavedConfirmationMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddEditRepairResult method is called with EDIT_RESULT_OK then ShowRefuelSavedConfirmationMessage should be sent`() = runTest {
        systemUnderTest.onAddEditRepairResult(EDIT_RESULT_OK)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.ShowRepairEditedConfirmationMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddNewRefuelClick method is called then NavigateToAddRefuelScreen should be sent`() = runTest {
        systemUnderTest.onAddNewRefuelClick()

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.NavigateToAddRefuelScreen
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onAddNewRepairClick method is called then NavigateToAddRefuelScreen should be sent`() = runTest {
        systemUnderTest.onAddNewRepairClick()

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.NavigateToAddRepairScreen
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onRepairItemSelected method is called then NavigateToEditRepairScreen should be sent`() = runTest {
        systemUnderTest.onRepairItemSelected(testRepair)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.NavigateToEditRepairScreen(testRepair)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `when onRefuelItemSelected method is called then NavigateToEditRefuelScreen should be sent`() = runTest {
        systemUnderTest.onRefuelItemSelected(testRefuel)

        systemUnderTest.refuelAndRepairEvent.test {
            awaitItem() shouldBe HistoryViewModel.HistoryEvent.NavigateToEditRefuelScreen(testRefuel)
            cancelAndConsumeRemainingEvents()
        }
    }
}