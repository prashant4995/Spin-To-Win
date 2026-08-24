package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.LocalFestiveSoundManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Dish
import com.example.model.SectorType
import com.example.model.WheelSector
import com.example.ui.components.DishIllustration
import com.example.ui.components.LuckyWheel
import com.example.ui.components.MarigoldGarland
import com.example.ui.components.ParticleConfetti
import com.example.ui.theme.ArtisticAmberContainer
import com.example.ui.theme.ArtisticAmberGlow
import com.example.ui.theme.ArtisticAmberGold
import com.example.ui.theme.ArtisticAmberSubtle
import com.example.ui.theme.ArtisticCream
import com.example.ui.theme.ArtisticCreamSub
import com.example.ui.theme.ArtisticMaroonBg
import com.example.ui.theme.ArtisticMaroonCard
import com.example.ui.theme.ArtisticMaroonDark
import com.example.ui.theme.ArtisticMaroonSurface
import com.example.ui.theme.FestiveCardBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SpinWheelScreen(
    userName: String,
    selectedDish: Dish?,
    sectors: List<WheelSector>,
    currentRotationAngle: Float,
    isSpinning: Boolean,
    totalSpins: Int,
    totalWins: Int,
    onBackToSelection: () -> Unit,
    onStartSpin: (onTargetCalculated: (Float) -> Unit) -> Unit,
    onSpinAnimationFinished: (Float) -> Unit,
    onOpenHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val soundManager = LocalFestiveSoundManager.current
    val wheelAnimatable = remember { Animatable(currentRotationAngle) }
    val scrollState = rememberScrollState()

    var confettiTriggerKey by remember { mutableStateOf<Long?>(null) }
    var isWinCelebrationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentRotationAngle) {
        if (!isSpinning) {
            wheelAnimatable.snapTo(currentRotationAngle)
        }
    }

    val triggerSpin = {
        if (!isSpinning) {
            isWinCelebrationVisible = false
            onStartSpin { targetAngle ->
                coroutineScope.launch {
                    wheelAnimatable.animateTo(
                        targetValue = targetAngle,
                        animationSpec = tween(
                            durationMillis = 4200,
                            easing = FastOutSlowInEasing
                        )
                    )

                    // Calculate landed sector accurately
                    val normalizedAngle = (270f - (targetAngle % 360f) + 360f) % 360f
                    val sectorIndex = ((normalizedAngle / 90f).toInt()) % sectors.size
                    val landedSector = sectors.getOrNull(sectorIndex) ?: sectors[0]
                    val isWin = landedSector.type == SectorType.WIN

                    if (isWin) {
                        // Automatically trigger particle confetti on the spin wheel screen
                        confettiTriggerKey = System.currentTimeMillis()
                        isWinCelebrationVisible = true
                        soundManager?.playWinChime()

                        // Allow the celebratory particle cascade to explode over the wheel
                        delay(2600)
                    }

                    onSpinAnimationFinished(targetAngle)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ArtisticMaroonBg,
                        ArtisticMaroonDark,
                        Color(0xFF1B0101)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Decorative Garland
            MarigoldGarland(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            // Top Greeting & Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                color = ArtisticMaroonCard,
                border = BorderStroke(1.dp, FestiveCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackToSelection,
                            enabled = !isSpinning,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ArtisticMaroonDark)
                                .testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to food selection",
                                tint = ArtisticAmberGold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Namaste, $userName! 🎊",
                                color = ArtisticAmberGold,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Spin to win free ${selectedDish?.title ?: "delicacy"}!",
                                color = ArtisticCreamSub,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Stat Badges, History & Sound Toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                soundManager?.playClickSound()
                                onOpenHistory()
                            },
                            enabled = !isSpinning,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ArtisticMaroonDark)
                                .testTag("history_nav_wheel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "View Winnings & History",
                                tint = ArtisticAmberGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        val soundManager = LocalFestiveSoundManager.current
                        val isMuted by (soundManager?.isMuted ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsStateWithLifecycle(false)

                        IconButton(
                            onClick = { soundManager?.toggleMute() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ArtisticMaroonDark)
                                .testTag("sound_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute sound" else "Mute sound",
                                tint = if (isMuted) ArtisticCreamSub else ArtisticAmberGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        StatPill(label = "Spins", value = "$totalSpins")
                        StatPill(label = "Wins", value = "$totalWins", isHighlight = true)
                    }
                }
            }

            // Main Portrait Content (Scrollable)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 3D Lucky Wheel Centerpiece
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LuckyWheel(
                        selectedDish = selectedDish,
                        sectors = sectors,
                        currentRotationAngle = wheelAnimatable.value,
                        isSpinning = isSpinning,
                        onSpinClick = triggerSpin,
                        onSpinComplete = onSpinAnimationFinished,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Target Prize Info Card
                selectedDish?.let { dish ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ArtisticMaroonCard,
                        border = BorderStroke(1.2.dp, ArtisticAmberGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                DishIllustration(
                                    dish = dish,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TARGET PRIZE: FREE ${dish.title.uppercase()}",
                                    color = ArtisticAmberGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "${dish.nativeTitle} • ${dish.tag}",
                                    color = ArtisticCream,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Land on either prize sector to win an instant claim ticket!",
                                    color = ArtisticCreamSub,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Fair Odds Note
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArtisticMaroonDark,
                    border = BorderStroke(0.8.dp, ArtisticAmberSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = ArtisticAmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3D Physics Engine: 2 Win Sectors • 2 Retry Sectors • Pure Random Deceleration",
                            color = ArtisticCreamSub,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Big 3D Tactile SPIN CTA Button
                Button(
                    onClick = triggerSpin,
                    enabled = !isSpinning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(if (!isSpinning) 10.dp else 0.dp, RoundedCornerShape(18.dp))
                        .testTag("spin_now_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArtisticAmberGold,
                        contentColor = ArtisticMaroonBg,
                        disabledContainerColor = Color(0xFF381A1A),
                        disabledContentColor = Color(0xFF7A4A4A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSpinning) Icons.Default.Redo else Icons.Default.Casino,
                            contentDescription = "Spin",
                            tint = if (isSpinning) Color(0xFF7A4A4A) else ArtisticMaroonBg,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isSpinning) "SPINNING 3D WHEEL..." else "SPIN THE 3D WHEEL NOW",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Particle-based Confetti Animation Overlay on Canvas
        if (confettiTriggerKey != null) {
            ParticleConfetti(
                trigger = confettiTriggerKey,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("win_confetti_canvas"),
                particleCount = 160,
                isCenterBurst = true,
                isDualCannons = true,
                isRainCascade = true
            )
        }

        // Celebratory Win Notification Banner on the Spin Wheel Screen
        AnimatedVisibility(
            visible = isWinCelebrationVisible,
            enter = fadeIn(tween(250)) + scaleIn(tween(350, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(300)) + scaleOut(tween(250)),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ArtisticMaroonDark,
                border = BorderStroke(2.5.dp, ArtisticAmberGold),
                shadowElevation = 24.dp,
                modifier = Modifier.shadow(24.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ArtisticMaroonCard,
                                    ArtisticMaroonDark
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = "Celebration",
                        tint = ArtisticAmberGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "🎉 JACKPOT WINNER! 🎉",
                        color = ArtisticAmberGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Congratulations! You won free ${selectedDish?.title ?: "Festive Delicacy"}!",
                        color = ArtisticCream,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isHighlight) ArtisticAmberContainer else ArtisticMaroonDark,
        border = BorderStroke(1.dp, if (isHighlight) ArtisticAmberGold else ArtisticAmberSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                color = if (isHighlight) ArtisticAmberGold else ArtisticCreamSub,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = if (isHighlight) ArtisticCream else ArtisticAmberGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
