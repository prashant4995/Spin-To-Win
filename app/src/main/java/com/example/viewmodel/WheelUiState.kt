package com.example.viewmodel

import com.example.data.local.ColorPalette
import com.example.data.local.SpinHistoryEntity
import com.example.data.local.ThemeMode
import com.example.model.AppScreen
import com.example.model.Dish
import com.example.model.OrderItem
import com.example.model.QualityOption
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
    val dishQuantities: Map<Dish, Int> = mapOf(
        Dish.MODAK to 1,
        Dish.KHANDVI to 1,
        Dish.COMBO_PLATE to 0
    ),
    val selectedDish: Dish? = Dish.MODAK,
    val selectedQualityOption: QualityOption = Dish.MODAK.defaultQualityOption,
    val quantity: Int = 2, // Total number of quantity requested
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
    val isPaymentSuccess: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val colorPalette: ColorPalette = ColorPalette.PEACOCK,
    val showThemeSettingsDialog: Boolean = false
) {
    val totalOrderQuantity: Int
        get() = dishQuantities.values.sum()

    val canProceedToSpin: Boolean
        get() = totalOrderQuantity >= 1

    val activeOrderItems: List<OrderItem>
        get() = dishQuantities.filter { it.value > 0 }.map { (dish, qty) ->
            OrderItem(dish = dish, quantity = qty, unitPrice = dish.pricePerUnit)
        }

    val primarySelectedDish: Dish
        get() = activeOrderItems.firstOrNull()?.dish ?: selectedDish ?: Dish.MODAK

    /**
     * Checks if the order consists exclusively of Festival Combos (and no other delicacies).
     */
    val isOnlyFestivalCombos: Boolean
        get() = (dishQuantities[Dish.COMBO_PLATE] ?: 0) > 0 &&
                dishQuantities.filterKeys { it != Dish.COMBO_PLATE }.values.all { it == 0 }

    /**
     * Spin & Win is unlocked when total order quantity is > 2, UNLESS the order consists
     * exclusively of Festival Combos (Festival Combo only orders proceed directly to checkout without Spin & Win).
     */
    val isLuckySpinUnlocked: Boolean
        get() = totalOrderQuantity > 2 && !isOnlyFestivalCombos

    /**
     * Winning dish logic for eligible lucky spins.
     * Prefers the individual festival delicacies (Modak / Khandvi) in the order.
     */
    val winningDishForSpin: Dish
        get() = activeOrderItems.firstOrNull { it.dish != Dish.COMBO_PLATE }?.dish
            ?: primarySelectedDish

    val effectiveUnitPrice: Int
        get() = primarySelectedDish.pricePerUnit

    val currentTotalAmount: Int
        get() = activeOrderItems.sumOf { it.totalPrice }

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

    val comboSoldCount: Int
        get() = historyList.filter { it.isSold && it.dishName?.contains("Combo", ignoreCase = true) == true }.sumOf { it.quantity }

    val comboFreeCount: Int
        get() = historyList.filter { (it.isFree || it.isWin) && it.dishName?.contains("Combo", ignoreCase = true) == true }.sumOf { it.quantity }
}

