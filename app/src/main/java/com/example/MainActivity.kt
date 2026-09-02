package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.FestiveSoundManager
import com.example.audio.LocalFestiveSoundManager
import com.example.model.AppScreen
import com.example.ui.components.DiyaLamp
import com.example.ui.components.ThemeSettingsDialog
import com.example.ui.screens.FoodSelectionScreen
import com.example.ui.screens.RewardResultScreen
import com.example.ui.screens.SpinHistoryScreen
import com.example.ui.screens.SpinWheelScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ArtisticAmberGold
import com.example.ui.theme.ArtisticCream
import com.example.ui.theme.ArtisticCreamSub
import com.example.ui.theme.ArtisticMaroonBg
import com.example.ui.theme.ArtisticMaroonCard
import com.example.ui.theme.ArtisticMaroonDark
import com.example.ui.theme.FestiveCardBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WheelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val soundManager = remember { FestiveSoundManager.getInstance(context) }
            val application = context.applicationContext as? Application ?: application
            val viewModel: WheelViewModel = viewModel(
                factory = WheelViewModel.Factory(application)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalFestiveSoundManager provides soundManager) {
                MyApplicationTheme(
                    themeMode = uiState.themeMode,
                    colorPalette = uiState.colorPalette
                ) {
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
    viewModel: WheelViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    // Screen-specific Back Handlers
    when (uiState.currentScreen) {
        AppScreen.Registration -> {
            BackHandler(enabled = true) {
                showExitConfirmationDialog = true
            }
        }
        AppScreen.SpinWheel -> {
            BackHandler(enabled = true) {
                soundManager?.stopSpinSound()
                soundManager?.playClickSound()
                viewModel.navigateTo(AppScreen.Registration)
            }
        }
        AppScreen.RewardResult -> {
            BackHandler(enabled = true) {
                soundManager?.playClickSound()
                viewModel.claimAndReset()
            }
        }
        AppScreen.History -> {
            BackHandler(enabled = true) {
                soundManager?.playClickSound()
                viewModel.closeHistory()
            }
        }
    }

    // Theme Settings Dialog
    if (uiState.showThemeSettingsDialog) {
        ThemeSettingsDialog(
            currentThemeMode = uiState.themeMode,
            currentColorPalette = uiState.colorPalette,
            onThemeModeSelected = viewModel::setThemeMode,
            onColorPaletteSelected = viewModel::setColorPalette,
            onDismiss = viewModel::closeThemeSettings
        )
    }

    // Dashboard Exit Confirmation Dialog
    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            containerColor = customColors.cardBg,
            titleContentColor = customColors.primaryAccent,
            textContentColor = customColors.textPrimary,
            icon = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiyaLamp(modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit",
                        tint = customColors.primaryAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DiyaLamp(modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text(
                    text = "Exit Festival Stall App?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "बाहेर पडायचे आहे का?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = customColors.primaryAccent,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Are you sure you want to exit the Ganesh Utsav Stall app? All recorded sales and history remain safely saved.",
                        fontSize = 13.sp,
                        color = customColors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmationDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.primaryAccent,
                        contentColor = customColors.textOnAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_exit_button")
                ) {
                    Text(
                        text = "EXIT APP",
                        color = customColors.textOnAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitConfirmationDialog = false },
                    border = BorderStroke(1.dp, customColors.primaryAccent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = customColors.primaryAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_exit_button")
                ) {
                    Text(
                        text = "CANCEL",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(16.dp)
                .testTag("exit_confirmation_dialog")
        )
    }

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
                    dishQuantities = uiState.dishQuantities,
                    totalQuantity = uiState.totalOrderQuantity,
                    totalOrderAmount = uiState.currentTotalAmount,
                    canProceed = uiState.canProceedToSpin,
                    onNameChanged = viewModel::updateName,
                    onDishQuantityChanged = viewModel::setDishQuantity,
                    onIncrementDishQuantity = viewModel::incrementDishQuantity,
                    onDecrementDishQuantity = viewModel::decrementDishQuantity,
                    onProceedClicked = {
                        soundManager?.playClickSound()
                        viewModel.proceedToSpin()
                    },
                    onOpenHistory = {
                        soundManager?.playClickSound()
                        viewModel.openHistory()
                    },
                    onOpenSettings = {
                        soundManager?.playClickSound()
                        viewModel.openThemeSettings()
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
                    },
                    onOpenSettings = {
                        soundManager?.playClickSound()
                        viewModel.openThemeSettings()
                    }
                )
            }

            AppScreen.RewardResult -> {
                RewardResultScreen(
                    result = uiState.lastResult,
                    selectedDish = uiState.selectedDish,
                    quantity = uiState.quantity,
                    isPaidViaQr = uiState.isPaymentSuccess,
                    onMarkPaidViaQr = {
                        soundManager?.playClaimChime()
                        viewModel.recordPaymentViaQr()
                    },
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
                    },
                    onOpenSettings = {
                        soundManager?.playClickSound()
                        viewModel.openThemeSettings()
                    }
                )
            }

            AppScreen.History -> {
                SpinHistoryScreen(
                    historyList = uiState.historyList,
                    filterType = uiState.historyFilterType,
                    totalSpins = uiState.totalSpins,
                    totalWins = uiState.totalWins,
                    totalItemsSold = uiState.totalItemsSold,
                    totalItemsFree = uiState.totalItemsFree,
                    totalRevenue = uiState.totalRevenue,
                    modakSoldCount = uiState.modakSoldCount,
                    modakFreeCount = uiState.modakFreeCount,
                    khandviSoldCount = uiState.khandviSoldCount,
                    khandviFreeCount = uiState.khandviFreeCount,
                    onFilterTypeChanged = viewModel::setHistoryFilterType,
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
                    },
                    onOpenSettings = {
                        soundManager?.playClickSound()
                        viewModel.openThemeSettings()
                    }
                )
            }
        }
    }
}
