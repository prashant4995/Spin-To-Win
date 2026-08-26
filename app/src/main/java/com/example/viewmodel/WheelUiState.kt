package com.example.viewmodel

import com.example.data.local.SpinHistoryEntity
import com.example.model.AppScreen
import com.example.model.Dish
import com.example.model.SpinResult
import com.example.model.WheelSector

enum class HistoryFilterType {
    ALL,
    SOLD,
    FREE
}

data class WheelUiState(
    val userName: String = "",
    val nameError: String? = null,
    val selectedDish: Dish? = null,
    val quantity: Int = 1, // Number of quantity requested (1..20)
    val currentScreen: AppScreen = AppScreen.Registration,
    val previousScreenForHistory: AppScreen = AppScreen.Registration,
    val isSpinning: Boolean = false,
    val currentRotationAngle: Float = 0f,
    val targetRotationAngle: Float = 0f,
    val targetSector: WheelSector? = null,
    val lastResult: SpinResult? = null,
    val sectors: List<WheelSector> = WheelSector.createDefaultSectors(),
    val totalSpins: Int = 0,
    val totalWins: Int = 0,
    val totalItemsSold: Int = 0,
    val totalItemsFree: Int = 0,
    val totalRevenue: Int = 0,
    val historyList: List<SpinHistoryEntity> = emptyList(),
    val historyFilterType: HistoryFilterType = HistoryFilterType.ALL,
    val showPaymentQrModal: Boolean = false,
    val isPaymentSuccess: Boolean = false
) {
    val canProceedToSpin: Boolean
        get() = userName.trim().isNotEmpty() && selectedDish != null && quantity >= 1

    val currentTotalAmount: Int
        get() = (selectedDish?.pricePerUnit ?: 30) * quantity

    val filteredHistory: List<SpinHistoryEntity>
        get() = when (historyFilterType) {
            HistoryFilterType.ALL -> historyList
            HistoryFilterType.SOLD -> historyList.filter { it.isSold }
            HistoryFilterType.FREE -> historyList.filter { it.isFree || it.isWin }
        }

    val modakSoldCount: Int
        get() = historyList.filter { it.isSold && it.dishName.equals("Modak", ignoreCase = true) }.sumOf { it.quantity }

    val modakFreeCount: Int
        get() = historyList.filter { (it.isFree || it.isWin) && it.dishName.equals("Modak", ignoreCase = true) }.sumOf { it.quantity }

    val khandviSoldCount: Int
        get() = historyList.filter { it.isSold && (it.dishName.equals("Khandvi", ignoreCase = true) || it.dishName.equals("Khandavi", ignoreCase = true) || it.dishName.equals("Kothimbir Vadi", ignoreCase = true)) }.sumOf { it.quantity }

    val khandviFreeCount: Int
        get() = historyList.filter { (it.isFree || it.isWin) && (it.dishName.equals("Khandvi", ignoreCase = true) || it.dishName.equals("Khandavi", ignoreCase = true) || it.dishName.equals("Kothimbir Vadi", ignoreCase = true)) }.sumOf { it.quantity }
}


