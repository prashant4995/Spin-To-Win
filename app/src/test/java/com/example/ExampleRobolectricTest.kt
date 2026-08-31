package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SpinHistoryEntity
import com.example.model.AppScreen
import com.example.model.Dish
import com.example.model.SectorType
import com.example.model.WheelSector
import com.example.viewmodel.HistoryFilterType
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
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = WheelViewModel(app)
        assertTrue(viewModel.uiState.value.canProceedToSpin) // Default dish Khandvi selected

        // Verify prices
        assertEquals(40, Dish.MODAK.pricePerUnit)
        assertEquals(30, Dish.KHANDVI.pricePerUnit)
        assertEquals(55, Dish.COMBO_PLATE.pricePerUnit)

        // Enter Name
        viewModel.updateName("Aarav Patel")
        assertEquals("Aarav Patel", viewModel.uiState.value.userName)
        assertTrue(viewModel.uiState.value.canProceedToSpin)

        // Select Combo Plate
        viewModel.selectDish(Dish.COMBO_PLATE)
        assertEquals(Dish.COMBO_PLATE, viewModel.uiState.value.selectedDish)
        assertTrue(viewModel.uiState.value.canProceedToSpin)

        // Select Modak with quantity = 1 (Direct Checkout flow)
        viewModel.selectDish(Dish.MODAK)
        viewModel.setQuantity(1)
        assertEquals(Dish.MODAK, viewModel.uiState.value.selectedDish)
        assertTrue(viewModel.uiState.value.canProceedToSpin)

        // Proceed with quantity 1 -> Direct checkout / payment
        viewModel.proceedToSpin()
        assertEquals(AppScreen.RewardResult, viewModel.uiState.value.currentScreen)
        assertTrue(viewModel.uiState.value.lastResult?.isDirectCheckout == true)

        // Reset to Registration and set quantity > 2 (e.g. 3) -> Lucky Spin flow
        viewModel.navigateTo(AppScreen.Registration)
        viewModel.setQuantity(3)
        viewModel.proceedToSpin()
        assertEquals(AppScreen.SpinWheel, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `test spin and reward flow`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = WheelViewModel(app)
        viewModel.updateName("Pooja Kadam")
        viewModel.selectDish(Dish.MODAK)
        viewModel.setQuantity(3) // Quantity > 2 unlocks lucky spin
        viewModel.proceedToSpin()
        assertEquals(AppScreen.SpinWheel, viewModel.uiState.value.currentScreen)

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
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = WheelViewModel(app)
        viewModel.navigateTo(AppScreen.SpinWheel)
        viewModel.openHistory()
        assertEquals(AppScreen.History, viewModel.uiState.value.currentScreen)

        viewModel.setHistoryFilterType(HistoryFilterType.SOLD)
        assertEquals(HistoryFilterType.SOLD, viewModel.uiState.value.historyFilterType)

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
            isFree = true,
            isSold = false,
            dishName = Dish.MODAK.title,
            dishNativeTitle = Dish.MODAK.nativeTitle,
            dishSubtitle = Dish.MODAK.subtitle,
            dishEmoji = Dish.MODAK.emoji,
            quantity = 1,
            totalAmount = 0,
            timestamp = System.currentTimeMillis()
        )
        val item2 = SpinHistoryEntity(
            userName = "Aarav",
            isWin = false,
            isFree = false,
            isSold = true,
            dishName = Dish.KHANDVI.title,
            dishNativeTitle = Dish.KHANDVI.nativeTitle,
            dishSubtitle = Dish.KHANDVI.subtitle,
            dishEmoji = Dish.KHANDVI.emoji,
            quantity = 2,
            totalAmount = 40,
            timestamp = System.currentTimeMillis() + 1000
        )

        dao.insertSpin(item1)
        dao.insertSpin(item2)

        val all = dao.getAllHistory().first()
        assertEquals(2, all.size)

        val wins = dao.getFreeWinningsHistory().first()
        assertEquals(1, wins.size)
        assertEquals(Dish.MODAK.title, wins[0].dishName)

        val sold = dao.getSoldHistory().first()
        assertEquals(1, sold.size)
        assertEquals(40, sold[0].totalAmount)

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
        val sectors = WheelSector.createDefaultSectors()
        assertEquals(4, sectors.size)
        val winSectors = sectors.filter { it.type == SectorType.WIN }
        assertEquals(1, winSectors.size)
        val tryAgainSectors = sectors.filter { it.type == SectorType.TRY_AGAIN }
        assertEquals(3, tryAgainSectors.size)
    }
}

