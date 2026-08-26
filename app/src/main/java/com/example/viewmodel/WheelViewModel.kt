package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SpinHistoryEntity
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
    private val repository: SpinHistoryRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        SpinHistoryRepository(AppDatabase.getDatabase(application).spinHistoryDao())
    )

    constructor() : this(
        Application(),
        SpinHistoryRepository(
            object : com.example.data.local.SpinHistoryDao {
                private val listFlow = kotlinx.coroutines.flow.MutableStateFlow<List<SpinHistoryEntity>>(emptyList())
                override fun getAllHistory(): kotlinx.coroutines.flow.Flow<List<SpinHistoryEntity>> = listFlow
                override fun getWinningsHistory(): kotlinx.coroutines.flow.Flow<List<SpinHistoryEntity>> =
                    kotlinx.coroutines.flow.MutableStateFlow(emptyList<SpinHistoryEntity>())
                override fun getTotalSpinsCount() = kotlinx.coroutines.flow.MutableStateFlow(0)
                override fun getTotalWinsCount() = kotlinx.coroutines.flow.MutableStateFlow(0)
                override suspend fun insertSpin(spin: SpinHistoryEntity): Long {
                    listFlow.update { listOf(spin) + it }
                    return 1L
                }
                override suspend fun deleteById(id: Long) {
                    listFlow.update { it.filterNot { item -> item.id == id } }
                }
                override suspend fun clearAllHistory() {
                    listFlow.value = emptyList()
                }
            }
        )
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

    private val _uiState = MutableStateFlow(WheelUiState())
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    init {
        // Collect persistent spin history from Room Database
        viewModelScope.launch {
            repository.allHistory.collect { history ->
                val calculatedWins = history.count { it.isWin }
                _uiState.update { current ->
                    current.copy(
                        historyList = history,
                        totalSpins = maxOf(current.totalSpins, history.size),
                        totalWins = maxOf(current.totalWins, calculatedWins)
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

    fun proceedToSpin() {
        val state = _uiState.value
        if (state.userName.trim().isEmpty()) {
            _uiState.update { it.copy(nameError = "Please enter your name to continue") }
            return
        }
        if (state.selectedDish == null) {
            return
        }

        _uiState.update {
            it.copy(
                nameError = null,
                currentScreen = AppScreen.SpinWheel
            )
        }
    }

    /**
     * Calculates the spin target angle and sets up the spin animation.
     * Guaranteed to stop accurately at a chosen sector.
     */
    fun startSpin(onTargetCalculated: (targetAngle: Float) -> Unit) {
        val state = _uiState.value
        if (state.isSpinning) return

        // Alternating win/try again (50% base win probability for exciting festival experience)
        // Choose target sector: 0 (Win), 1 (Try again), 2 (Win), 3 (Try again)
        val targetSectorIndex = Random.nextInt(0, 4)
        val targetSector = state.sectors[targetSectorIndex]

        val sectorCenterAngle = (targetSectorIndex * 90f + 45f)
        val requiredMod = (270f - sectorCenterAngle + 360f) % 360f

        // Random jitter inside the sector: -22° to +22° (well inside 90° arc)
        val jitter = Random.nextFloat() * 44f - 22f

        // Full rotations: between 5 and 7 complete spins for high drama
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
        val claimCode = if (isWin) generateClaimCode() else ""
        val wonDish = if (isWin) state.selectedDish else null
        val guestName = state.userName.trim().ifEmpty { "Festive Guest" }

        val result = SpinResult(
            isWin = isWin,
            wonDish = wonDish,
            userName = guestName,
            claimCode = claimCode
        )

        // Persist spin outcome to Room Database
        viewModelScope.launch {
            val entity = SpinHistoryEntity(
                userName = guestName,
                isWin = isWin,
                dishName = wonDish?.title,
                dishNativeTitle = wonDish?.nativeTitle,
                dishSubtitle = wonDish?.subtitle,
                dishEmoji = wonDish?.emoji,
                claimCode = claimCode,
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
                currentScreen = AppScreen.RewardResult
            )
        }
    }

    fun spinAgain() {
        _uiState.update { current ->
            current.copy(
                currentScreen = AppScreen.SpinWheel,
                isSpinning = false,
                lastResult = null
            )
        }
    }

    fun claimAndReset() {
        _uiState.update { current ->
            WheelUiState(
                historyList = current.historyList,
                totalSpins = current.totalSpins,
                totalWins = current.totalWins
            )
        }
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

    fun setHistoryFilterOnlyWins(onlyWins: Boolean) {
        _uiState.update { it.copy(historyFilterOnlyWins = onlyWins) }
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

    private fun generateClaimCode(): String {
        val codeChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val prefix = "LUCKY"
        val suffix = (1..4).map { codeChars.random() }.joinToString("")
        return "$prefix-$suffix"
    }
}

