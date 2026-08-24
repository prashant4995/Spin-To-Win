package com.example.viewmodel

import com.example.data.local.SpinHistoryEntity
import com.example.model.AppScreen
import com.example.model.Dish
import com.example.model.SpinResult
import com.example.model.WheelSector

data class WheelUiState(
    val userName: String = "",
    val nameError: String? = null,
    val selectedDish: Dish? = null,
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
    val historyList: List<SpinHistoryEntity> = emptyList(),
    val historyFilterOnlyWins: Boolean = false
) {
    val canProceedToSpin: Boolean
        get() = userName.trim().isNotEmpty() && selectedDish != null

    val filteredHistory: List<SpinHistoryEntity>
        get() = if (historyFilterOnlyWins) {
            historyList.filter { it.isWin }
        } else {
            historyList
        }
}

