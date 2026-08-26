package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SpinHistoryEntity
import com.example.model.AppScreen
import com.example.model.Dish
import com.example.viewmodel.WheelViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lucky Spin & Win", appName)
    }

    @Test
    fun `test initial state and dish selection flow`() {
        val viewModel = WheelViewModel()
        assertFalse(viewModel.uiState.value.canProceedToSpin)

        // Enter Name
        viewModel.updateName("Aarav Patel")
        assertEquals("Aarav Patel", viewModel.uiState.value.userName)
        assertFalse(viewModel.uiState.value.canProceedToSpin)

        // Select Dish
        viewModel.selectDish(Dish.KOTHIMBIR_VADI)
        assertEquals(Dish.KOTHIMBIR_VADI, viewModel.uiState.value.selectedDish)
        assertTrue(viewModel.uiState.value.canProceedToSpin)

        // Proceed to Spin
        viewModel.proceedToSpin()
        assertEquals(AppScreen.SpinWheel, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `test spin and reward flow`() {
        val viewModel = WheelViewModel()
        viewModel.updateName("Pooja Kadam")
        viewModel.selectDish(Dish.MODAK)
        viewModel.proceedToSpin()

        var calculatedTarget = 0f
        viewModel.startSpin { targetAngle ->
            calculatedTarget = targetAngle
        }

        assertTrue(viewModel.uiState.value.isSpinning)
        assertTrue(calculatedTarget > 0f)

        // Complete spin animation
        viewModel.onSpinAnimationFinished(calculatedTarget)
        assertFalse(viewModel.uiState.value.isSpinning)
        assertEquals(AppScreen.RewardResult, viewModel.uiState.value.currentScreen)
        assertNotNull(viewModel.uiState.value.lastResult)
        assertTrue(viewModel.uiState.value.totalSpins >= 1)
    }

    @Test
    fun `test history navigation and filter toggle`() {
        val viewModel = WheelViewModel()
        viewModel.navigateTo(AppScreen.SpinWheel)
        viewModel.openHistory()
        assertEquals(AppScreen.History, viewModel.uiState.value.currentScreen)

        viewModel.setHistoryFilterOnlyWins(true)
        assertTrue(viewModel.uiState.value.historyFilterOnlyWins)

        viewModel.closeHistory()
        assertEquals(AppScreen.SpinWheel, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `test room database in memory spin history dao operations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.spinHistoryDao()

        val item1 = SpinHistoryEntity(
            userName = "Aarav",
            isWin = true,
            dishName = Dish.MODAK.title,
            dishNativeTitle = Dish.MODAK.nativeTitle,
            dishSubtitle = Dish.MODAK.subtitle,
            dishEmoji = Dish.MODAK.emoji,
            claimCode = "GANESH-MODAK-1234",
            timestamp = System.currentTimeMillis()
        )
        val item2 = SpinHistoryEntity(
            userName = "Aarav",
            isWin = false,
            dishName = Dish.KOTHIMBIR_VADI.title,
            dishNativeTitle = Dish.KOTHIMBIR_VADI.nativeTitle,
            dishSubtitle = Dish.KOTHIMBIR_VADI.subtitle,
            dishEmoji = Dish.KOTHIMBIR_VADI.emoji,
            claimCode = "",
            timestamp = System.currentTimeMillis() + 1000
        )

        dao.insertSpin(item1)
        dao.insertSpin(item2)

        val all = dao.getAllHistory().first()
        assertEquals(2, all.size)

        val wins = dao.getWinningsHistory().first()
        assertEquals(1, wins.size)
        assertEquals("GANESH-MODAK-1234", wins[0].claimCode)

        dao.deleteById(all[0].id)
        val remaining = dao.getAllHistory().first()
        assertEquals(1, remaining.size)

        dao.clearAllHistory()
        val empty = dao.getAllHistory().first()
        assertEquals(0, empty.size)

        db.close()
    }

    @Test
    fun `test wheel sector configuration and win sectors`() {
        val sectors = com.example.model.WheelSector.createDefaultSectors()
        assertEquals(4, sectors.size)
        val winSectors = sectors.filter { it.type == com.example.model.SectorType.WIN }
        assertEquals(2, winSectors.size)
        val retrySectors = sectors.filter { it.type == com.example.model.SectorType.TRY_AGAIN }
        assertEquals(2, retrySectors.size)
    }
}

