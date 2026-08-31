package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ColorPalette
import com.example.data.local.SpinHistoryEntity
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePreferences
import com.example.data.repository.SpinHistoryRepository
import com.example.model.AppScreen
import com.example.model.Dish
import com.example.model.SectorType
import com.example.model.SpinResult
import com.example.model.WheelSector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class WheelViewModel(
    application: Application,
    private val repository: SpinHistoryRepository,
    private val themePreferences: ThemePreferences = ThemePreferences.getInstance(application)
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        SpinHistoryRepository(AppDatabase.getDatabase(application).spinHistoryDao()),
        ThemePreferences.getInstance(application)
    )

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WheelViewModel::class.java)) {
                return WheelViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private val _uiState = MutableStateFlow(
        WheelUiState(
            themeMode = themePreferences.themeSettings.value.mode,
            colorPalette = themePreferences.themeSettings.value.palette
        )
    )
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    init {
        // Collect persistent theme settings
        viewModelScope.launch {
            themePreferences.themeSettings.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        themeMode = settings.mode,
                        colorPalette = settings.palette
                    )
                }
            }
        }

        // Collect persistent history and compute sold/free item metrics in real-time
        viewModelScope.launch {
            repository.allHistory.collect { history ->
                val calculatedWins = history.count { it.isWin || it.isFree }
                val itemsSold = history.filter { it.isSold }.sumOf { it.quantity }
                val itemsFree = history.filter { it.isFree || it.isWin }.sumOf { it.quantity }
                val revenue = history.filter { it.isSold }.sumOf { it.totalAmount }

                _uiState.update { current ->
                    current.copy(
                        historyList = history,
                        totalSpins = maxOf(current.totalSpins, history.size),
                        totalWins = maxOf(current.totalWins, calculatedWins),
                        totalItemsSold = itemsSold,
                        totalItemsFree = itemsFree,
                        totalRevenue = revenue
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { current ->
            current.copy(
                userName = name,
                nameError = if (name.isNotBlank()) null else current.nameError
            )
        }
    }

    fun selectDish(dish: Dish) {
        _uiState.update { current ->
            current.copy(selectedDish = dish)
        }
    }

    fun setQuantity(qty: Int) {
        val safeQty = qty.coerceIn(1, 50)
        _uiState.update { it.copy(quantity = safeQty) }
    }

    fun incrementQuantity() {
        _uiState.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(50)) }
    }

    fun decrementQuantity() {
        _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }
    }

    fun proceedToSpin() {
        val state = _uiState.value
        val effectiveName = if (state.userName.trim().isEmpty()) "Guest" else state.userName.trim()
        val effectiveDish = state.selectedDish ?: Dish.KHANDVI

        if (state.quantity > 2) {
            // Quantity > 2 unlocks the 3D Lucky Spin
            _uiState.update {
                it.copy(
                    userName = effectiveName,
                    selectedDish = effectiveDish,
                    nameError = null,
                    currentScreen = AppScreen.SpinWheel
                )
            }
        } else {
            // Quantity <= 2 proceeds directly to checkout / payment
            val directResult = SpinResult(
                isWin = false,
                wonDish = null,
                quantity = state.quantity,
                userName = effectiveName,
                isSold = false,
                amountPaid = 0,
                isPaidViaQr = false,
                isDirectCheckout = true
            )
            _uiState.update {
                it.copy(
                    userName = effectiveName,
                    selectedDish = effectiveDish,
                    nameError = null,
                    lastResult = directResult,
                    currentScreen = AppScreen.RewardResult,
                    showPaymentQrModal = false,
                    isPaymentSuccess = false
                )
            }
        }
    }

    /**
     * Calculates the spin target angle and sets up the spin animation.
     */
    fun startSpin(onTargetCalculated: (targetAngle: Float) -> Unit) {
        val state = _uiState.value
        if (state.isSpinning) return

        // 1 in 15 Winning Probability (approx 6.67% win rate)
        val isWin = Random.nextInt(15) == 0

        val targetSectorIndex = if (isWin) {
            val winIndices = state.sectors.indices.filter { state.sectors[it].type == SectorType.WIN }
            if (winIndices.isNotEmpty()) winIndices.random() else 0
        } else {
            val nonWinIndices = state.sectors.indices.filter { state.sectors[it].type == SectorType.TRY_AGAIN }
            if (nonWinIndices.isNotEmpty()) nonWinIndices.random() else 1
        }

        val targetSector = state.sectors[targetSectorIndex]

        val sectorCenterAngle = (targetSectorIndex * 90f + 45f)
        val requiredMod = (270f - sectorCenterAngle + 360f) % 360f

        // Random jitter inside the sector: -22° to +22°
        val jitter = Random.nextFloat() * 44f - 22f

        // Full rotations: between 5 and 7 complete spins
        val fullRotations = (5 + Random.nextInt(3)) * 360f

        val currentAngle = state.currentRotationAngle
        val currentMod = currentAngle % 360f
        var additionalDeg = requiredMod - currentMod + jitter
        if (additionalDeg <= 0) {
            additionalDeg += 360f
        }
        val finalTargetAngle = currentAngle + fullRotations + additionalDeg

        _uiState.update {
            it.copy(
                isSpinning = true,
                targetRotationAngle = finalTargetAngle,
                targetSector = targetSector
            )
        }

        onTargetCalculated(finalTargetAngle)
    }

    fun onSpinAnimationFinished(finalAngle: Float) {
        val state = _uiState.value
        val sector = state.targetSector ?: state.sectors[0]
        val isWin = sector.type == SectorType.WIN
        val wonDish = if (isWin) state.selectedDish else null
        val guestName = state.userName.trim().ifEmpty { "Festive Guest" }
        val currentDish = state.selectedDish

        val result = SpinResult(
            isWin = isWin,
            wonDish = wonDish,
            quantity = state.quantity,
            userName = guestName,
            isSold = false,
            amountPaid = 0,
            isPaidViaQr = false
        )

        // Persist spin record to Room Database
        viewModelScope.launch {
            val entity = SpinHistoryEntity(
                userName = guestName,
                isWin = isWin,
                isSold = false,
                isFree = isWin,
                quantity = if (isWin) 1 else state.quantity,
                unitPrice = currentDish?.pricePerUnit ?: 30,
                totalAmount = 0,
                dishName = (wonDish ?: currentDish)?.title,
                dishNativeTitle = (wonDish ?: currentDish)?.nativeTitle,
                dishSubtitle = (wonDish ?: currentDish)?.subtitle,
                dishEmoji = (wonDish ?: currentDish)?.emoji,
                isPaidViaQr = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSpin(entity)
        }

        _uiState.update { current ->
            current.copy(
                isSpinning = false,
                currentRotationAngle = finalAngle,
                lastResult = result,
                totalSpins = current.totalSpins + 1,
                totalWins = if (isWin) current.totalWins + 1 else current.totalWins,
                currentScreen = AppScreen.RewardResult,
                showPaymentQrModal = false,
                isPaymentSuccess = false
            )
        }
    }

    /**
     * Complete payment via QR Code for sold items.
     * Records the purchase in Room Database as Sold item with revenue.
     */
    fun recordPaymentViaQr(customQty: Int? = null) {
        val state = _uiState.value
        if (state.isPaymentSuccess) return

        val dish = state.selectedDish ?: Dish.MODAK
        val isWin = state.lastResult?.isWin == true
        val paidQty = customQty ?: if (isWin) (state.quantity - 1).coerceAtLeast(0) else state.quantity
        val totalAmt = (dish.pricePerUnit) * paidQty
        val guestName = state.userName.trim().ifEmpty { "Festive Customer" }

        if (paidQty > 0) {
            viewModelScope.launch {
                val entity = SpinHistoryEntity(
                    userName = guestName,
                    isWin = false,
                    isSold = true,
                    isFree = false,
                    quantity = paidQty,
                    unitPrice = dish.pricePerUnit,
                    totalAmount = totalAmt,
                    dishName = dish.title,
                    dishNativeTitle = dish.nativeTitle,
                    dishSubtitle = dish.subtitle,
                    dishEmoji = dish.emoji,
                    isPaidViaQr = true,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertSpin(entity)
            }
        }

        _uiState.update { current ->
            current.copy(
                isPaymentSuccess = true,
                showPaymentQrModal = true,
                lastResult = current.lastResult?.copy(
                    isSold = true,
                    amountPaid = totalAmt,
                    isPaidViaQr = true
                )
            )
        }
    }

    fun openPaymentQrModal() {
        _uiState.update { it.copy(showPaymentQrModal = true) }
    }

    fun closePaymentQrModal() {
        _uiState.update { it.copy(showPaymentQrModal = false) }
    }

    fun spinAgain() {
        _uiState.update { current ->
            current.copy(
                currentScreen = AppScreen.SpinWheel,
                isSpinning = false,
                lastResult = null,
                showPaymentQrModal = false,
                isPaymentSuccess = false
            )
        }
    }

    /**
     * Finalizes the current order/spin:
     * Considers payment as DONE (automatically records any pending payment/sale in Room DB),
     * then resets state back to Dashboard/Registration.
     */
    fun claimAndReset() {
        val state = _uiState.value
        val dish = state.selectedDish
        val isWin = state.lastResult?.isWin == true
        val paidQty = if (isWin) (state.quantity - 1).coerceAtLeast(0) else state.quantity
        val totalAmt = if (dish != null) paidQty * dish.pricePerUnit else 0
        val guestName = state.userName.trim().ifEmpty { "Festive Customer" }

        // If payment was not already explicitly recorded, mark and record it as Payment Done now
        if (!state.isPaymentSuccess && dish != null && paidQty > 0) {
            viewModelScope.launch {
                val entity = SpinHistoryEntity(
                    userName = guestName,
                    isWin = false,
                    isSold = true,
                    isFree = false,
                    quantity = paidQty,
                    unitPrice = dish.pricePerUnit,
                    totalAmount = totalAmt,
                    dishName = dish.title,
                    dishNativeTitle = dish.nativeTitle,
                    dishSubtitle = dish.subtitle,
                    dishEmoji = dish.emoji,
                    isPaidViaQr = true,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertSpin(entity)
            }
        }

        _uiState.update { current ->
            WheelUiState(
                historyList = current.historyList,
                totalSpins = current.totalSpins,
                totalWins = current.totalWins,
                totalItemsSold = current.totalItemsSold,
                totalItemsFree = current.totalItemsFree,
                totalRevenue = current.totalRevenue,
                themeMode = current.themeMode,
                colorPalette = current.colorPalette,
                showThemeSettingsDialog = current.showThemeSettingsDialog
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    fun setColorPalette(palette: ColorPalette) {
        themePreferences.setColorPalette(palette)
    }

    fun openThemeSettings() {
        _uiState.update { it.copy(showThemeSettingsDialog = true) }
    }

    fun closeThemeSettings() {
        _uiState.update { it.copy(showThemeSettingsDialog = false) }
    }

    fun openHistory() {
        _uiState.update { current ->
            current.copy(
                previousScreenForHistory = current.currentScreen,
                currentScreen = AppScreen.History
            )
        }
    }

    fun closeHistory() {
        _uiState.update { current ->
            current.copy(currentScreen = current.previousScreenForHistory)
        }
    }

    fun setHistoryFilterType(filter: HistoryFilterType) {
        _uiState.update { it.copy(historyFilterType = filter) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }
}


