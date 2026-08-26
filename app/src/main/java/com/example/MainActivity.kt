package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.FestiveSoundManager
import com.example.audio.LocalFestiveSoundManager
import com.example.model.AppScreen
import com.example.ui.screens.FoodSelectionScreen
import com.example.ui.screens.RewardResultScreen
import com.example.ui.screens.SpinHistoryScreen
import com.example.ui.screens.SpinWheelScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WheelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val soundManager = remember { FestiveSoundManager.getInstance(context) }

            CompositionLocalProvider(LocalFestiveSoundManager provides soundManager) {
                MyApplicationTheme {
                    val application = context.applicationContext as? Application ?: application
                    val viewModel: WheelViewModel = viewModel(
                        factory = WheelViewModel.Factory(application)
                    )
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .safeDrawingPadding()
                        ) {
                            LuckySpinApp(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            FestiveSoundManager.getInstance(this).stopSpinSound()
        } catch (e: Exception) {
            // Safe cleanup
        }
    }
}

@Composable
fun LuckySpinApp(
    viewModel: WheelViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val soundManager = LocalFestiveSoundManager.current

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = {
            if (targetState is AppScreen.SpinWheel && initialState is AppScreen.Registration) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            } else if (targetState is AppScreen.Registration && initialState is AppScreen.SpinWheel) {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            } else if (targetState is AppScreen.History) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            } else if (initialState is AppScreen.History) {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            } else {
                fadeIn().togetherWith(fadeOut())
            }
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.Registration -> {
                FoodSelectionScreen(
                    userName = uiState.userName,
                    nameError = uiState.nameError,
                    selectedDish = uiState.selectedDish,
                    canProceed = uiState.canProceedToSpin,
                    onNameChanged = viewModel::updateName,
                    onDishSelected = { dish ->
                        soundManager?.playClickSound()
                        viewModel.selectDish(dish)
                    },
                    onProceedClicked = {
                        soundManager?.playClickSound()
                        viewModel.proceedToSpin()
                    },
                    onOpenHistory = {
                        soundManager?.playClickSound()
                        viewModel.openHistory()
                    }
                )
            }

            AppScreen.SpinWheel -> {
                SpinWheelScreen(
                    userName = uiState.userName,
                    selectedDish = uiState.selectedDish,
                    sectors = uiState.sectors,
                    currentRotationAngle = uiState.currentRotationAngle,
                    isSpinning = uiState.isSpinning,
                    totalSpins = uiState.totalSpins,
                    totalWins = uiState.totalWins,
                    onBackToSelection = {
                        soundManager?.stopSpinSound()
                        soundManager?.playClickSound()
                        viewModel.navigateTo(AppScreen.Registration)
                    },
                    onStartSpin = { onTargetCalculated ->
                        soundManager?.playSpinSound()
                        viewModel.startSpin(onTargetCalculated)
                    },
                    onSpinAnimationFinished = { finalAngle ->
                        soundManager?.stopSpinSound()
                        viewModel.onSpinAnimationFinished(finalAngle)
                    },
                    onOpenHistory = {
                        soundManager?.playClickSound()
                        viewModel.openHistory()
                    }
                )
            }

            AppScreen.RewardResult -> {
                RewardResultScreen(
                    result = uiState.lastResult,
                    selectedDish = uiState.selectedDish,
                    onClaimAndReset = {
                        soundManager?.playClaimChime()
                        viewModel.claimAndReset()
                    },
                    onSpinAgain = {
                        soundManager?.playClickSound()
                        viewModel.spinAgain()
                    },
                    onRestart = {
                        soundManager?.playClickSound()
                        viewModel.navigateTo(AppScreen.Registration)
                    },
                    onOpenHistory = {
                        soundManager?.playClickSound()
                        viewModel.openHistory()
                    }
                )
            }

            AppScreen.History -> {
                SpinHistoryScreen(
                    historyList = uiState.historyList,
                    filterOnlyWins = uiState.historyFilterOnlyWins,
                    totalSpins = uiState.totalSpins,
                    totalWins = uiState.totalWins,
                    onFilterChanged = viewModel::setHistoryFilterOnlyWins,
                    onDeleteHistoryItem = viewModel::deleteHistoryItem,
                    onClearAllHistory = viewModel::clearAllHistory,
                    onNavigateBack = {
                        soundManager?.playClickSound()
                        viewModel.closeHistory()
                    },
                    onStartSpin = {
                        soundManager?.playClickSound()
                        if (uiState.canProceedToSpin) {
                            viewModel.navigateTo(AppScreen.SpinWheel)
                        } else {
                            viewModel.navigateTo(AppScreen.Registration)
                        }
                    }
                )
            }
        }
    }
}
