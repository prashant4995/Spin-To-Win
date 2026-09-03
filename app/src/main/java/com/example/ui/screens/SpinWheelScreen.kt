package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.LocalFestiveSoundManager
import com.example.model.Dish
import com.example.model.SectorType
import com.example.model.WheelSector
import com.example.ui.animation.WheelPhysicsEngine
import com.example.ui.animation.WheelPhysicsState
import com.example.ui.animation.WheelSpinPhase
import com.example.ui.components.DishIllustration
import com.example.ui.components.DiyaLamp
import com.example.ui.components.LuckyWheel
import com.example.ui.components.MarigoldGarland
import com.example.ui.components.ParticleConfetti
import com.example.ui.theme.AppTheme
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
import kotlin.math.sin

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
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val soundManager = LocalFestiveSoundManager.current
    val scrollState = rememberScrollState()

    var physicsState by remember {
        mutableStateOf(
            WheelPhysicsState(
                currentAngle = currentRotationAngle,
                landedSectorIndex = WheelPhysicsEngine.calculateLandedSectorIndex(currentRotationAngle, sectors.size),
                landedSector = sectors.getOrNull(WheelPhysicsEngine.calculateLandedSectorIndex(currentRotationAngle, sectors.size))
            )
        )
    }

    var confettiTriggerKey by remember { mutableStateOf<Long?>(null) }
    var isWinCelebrationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentRotationAngle) {
        if (!isSpinning) {
            val landedIdx = WheelPhysicsEngine.calculateLandedSectorIndex(currentRotationAngle, sectors.size)
            physicsState = physicsState.copy(
                currentAngle = currentRotationAngle,
                angularVelocityDegPerSec = 0f,
                normalizedVelocity = 0f,
                pointerDeflectionAngle = 0f,
                phase = WheelSpinPhase.IDLE,
                landedSectorIndex = landedIdx,
                landedSector = sectors.getOrNull(landedIdx)
            )
        }
    }

    val triggerSpin = {
        if (!isSpinning) {
            isWinCelebrationVisible = false
            onStartSpin { targetAngle ->
                coroutineScope.launch {
                    val startAngle = physicsState.currentAngle
                    val totalDelta = targetAngle - startAngle
                    val durationMs = WheelPhysicsEngine.DEFAULT_SPIN_DURATION_MS
                    val startTime = System.currentTimeMillis()
                    var wasPegCrossing = false
                    var lastClickTime = 0L

                    soundManager?.playSpinSound()

                    while (true) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val rawProgress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                        val physicsProgress = WheelPhysicsEngine.calculatePhysicsProgress(rawProgress)
                        val currentAngle = startAngle + totalDelta * physicsProgress
                        val velocity = WheelPhysicsEngine.calculateAngularVelocity(rawProgress, totalDelta, durationMs)
                        val (pointerDeflection, isPegCrossing) = WheelPhysicsEngine.calculatePointerDeflection(currentAngle, velocity)
                        val landedIndex = WheelPhysicsEngine.calculateLandedSectorIndex(currentAngle, sectors.size)
                        val landedSector = sectors.getOrNull(landedIndex) ?: sectors[0]

                        // Real-time clicking sound during wheel rotation as pins hit flapper
                        if (isPegCrossing && !wasPegCrossing) {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime > 22L) {
                                lastClickTime = now
                                val speedFactor = (velocity / 1000f).coerceIn(0.5f, 1.4f)
                                soundManager?.playWheelClick(speedFactor)
                            }
                        }
                        wasPegCrossing = isPegCrossing

                        val phase = when {
                            rawProgress < WheelPhysicsEngine.PHASE_ACCEL_END -> WheelSpinPhase.ACCELERATION
                            rawProgress < WheelPhysicsEngine.PHASE_DECEL_END -> WheelSpinPhase.DECELERATION
                            rawProgress < 1.0f -> WheelSpinPhase.FINAL_LANDING
                            else -> WheelSpinPhase.LANDED
                        }

                        val landingPulseAlpha = if (phase == WheelSpinPhase.FINAL_LANDING || phase == WheelSpinPhase.LANDED) {
                            val t = ((elapsed - durationMs * WheelPhysicsEngine.PHASE_DECEL_END) / 1000f)
                            (0.5f + 0.5f * sin(t * 8f)).toFloat().coerceIn(0f, 1f)
                        } else 0f

                        physicsState = WheelPhysicsState(
                            currentAngle = currentAngle,
                            angularVelocityDegPerSec = velocity,
                            normalizedVelocity = (velocity / 1800f).coerceIn(0f, 1f),
                            phase = phase,
                            pointerDeflectionAngle = pointerDeflection,
                            isPegCrossing = isPegCrossing,
                            progress = rawProgress,
                            landedSectorIndex = landedIndex,
                            landedSector = landedSector,
                            isLandedPrizeWin = landedSector.type == SectorType.WIN,
                            landingPulseAlpha = landingPulseAlpha
                        )

                        if (rawProgress >= 1f) {
                            break
                        }

                        withFrameMillis { /* next frame */ }
                    }

                    // Precise settling at target angle
                    val finalLandedIndex = WheelPhysicsEngine.calculateLandedSectorIndex(targetAngle, sectors.size)
                    val finalSector = sectors.getOrNull(finalLandedIndex) ?: sectors[0]
                    val isWin = finalSector.type == SectorType.WIN

                    physicsState = physicsState.copy(
                        currentAngle = targetAngle,
                        angularVelocityDegPerSec = 0f,
                        normalizedVelocity = 0f,
                        pointerDeflectionAngle = 0f,
                        phase = WheelSpinPhase.LANDED,
                        landedSectorIndex = finalLandedIndex,
                        landedSector = finalSector,
                        isLandedPrizeWin = isWin,
                        landingPulseAlpha = 1f
                    )

                    if (isWin) {
                        confettiTriggerKey = System.currentTimeMillis()
                        isWinCelebrationVisible = true
                        soundManager?.playCelebrationSound()
                        soundManager?.announceWinner(userName, selectedDish?.title ?: "Prasad")
                        delay(2800)
                    } else {
                        soundManager?.playTryAgainSound()
                        delay(1600)
                    }

                    onSpinAnimationFinished(targetAngle)
                }
            }
        }
    }

    val customColors = AppTheme.customColors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        customColors.bg,
                        customColors.bgSurface,
                        customColors.cardBgSubtle
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight
        val isLandscapeTablet = isLandscape && maxWidth >= 900.dp
        val isTablet = maxWidth >= 600.dp
        val tabletSideScrollState = rememberScrollState()

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
                    .widthIn(max = 1000.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 6.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = customColors.cardBg,
                border = BorderStroke(1.dp, customColors.cardBorder)
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
                                .background(customColors.primaryAccent)
                                .testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to food selection",
                                tint = customColors.textOnAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Namaste, $userName! 🎊",
                                color = customColors.primaryAccent,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Spin to win free ${selectedDish?.title ?: "delicacy"}!",
                                color = customColors.textSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Stat Badges, History & Sound Toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                soundManager?.playClickSound()
                                onOpenSettings()
                            },
                            enabled = !isSpinning,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(customColors.surfaceDark)
                                .testTag("theme_settings_nav_wheel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme & Colors",
                                tint = customColors.primaryAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                soundManager?.playClickSound()
                                onOpenHistory()
                            },
                            enabled = !isSpinning,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(customColors.surfaceDark)
                                .testTag("history_nav_wheel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "View Winnings & History",
                                tint = customColors.primaryAccent,
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
                                .background(customColors.surfaceDark)
                                .testTag("sound_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute sound" else "Mute sound",
                                tint = if (isMuted) customColors.textSecondary else customColors.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        StatPill(label = "Spins", value = "$totalSpins")
                        StatPill(label = "Wins", value = "$totalWins", isHighlight = true)
                    }
                }
            }

            if (isLandscapeTablet) {
                // TABLET WIDESCREEN LANDSCAPE (Centered 2-Pane)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 1020.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Pane: Large Centered 3D Wheel
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .heightIn(max = 440.dp)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LuckyWheel(
                                selectedDish = selectedDish,
                                sectors = sectors,
                                currentRotationAngle = physicsState.currentAngle,
                                isSpinning = isSpinning,
                                pointerDeflectionAngle = physicsState.pointerDeflectionAngle,
                                phase = physicsState.phase,
                                angularVelocity = physicsState.angularVelocityDegPerSec,
                                landedSectorIndex = physicsState.landedSectorIndex,
                                landingPulseAlpha = physicsState.landingPulseAlpha,
                                onSpinClick = triggerSpin,
                                onSpinComplete = onSpinAnimationFinished,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Right Pane: Centered physics state, target prize, CTA button, fair-play note
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(tabletSideScrollState)
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SpinPhysicsStatusPill(physicsState = physicsState)

                            selectedDish?.let { dish ->
                                SpinTargetPrizeCard(dish = dish)
                            }

                            SpinActionButton(
                                isSpinning = isSpinning,
                                onClick = triggerSpin
                            )

                            // Fair Play / Blessing Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ArtisticMaroonCard,
                                border = BorderStroke(1.dp, ArtisticAmberSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    DiyaLamp(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Authentic 3D Physics • Physical friction, torque & harmonic detent locks",
                                        color = ArtisticCreamSub,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    DiyaLamp(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // PORTRAIT TABLET & PHONE LAYOUT (Centered Vertically and Horizontally)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = if (isTablet) 600.dp else 520.dp)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 14.dp)
                    ) {
                        // Real-time Physics Engine Status Pill
                        SpinPhysicsStatusPill(physicsState = physicsState)

                        // 3D Lucky Wheel Centerpiece (Centered in screen)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isTablet) 390.dp else 330.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LuckyWheel(
                                selectedDish = selectedDish,
                                sectors = sectors,
                                currentRotationAngle = physicsState.currentAngle,
                                isSpinning = isSpinning,
                                pointerDeflectionAngle = physicsState.pointerDeflectionAngle,
                                phase = physicsState.phase,
                                angularVelocity = physicsState.angularVelocityDegPerSec,
                                landedSectorIndex = physicsState.landedSectorIndex,
                                landingPulseAlpha = physicsState.landingPulseAlpha,
                                onSpinClick = triggerSpin,
                                onSpinComplete = onSpinAnimationFinished,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Target Prize Info Card
                        selectedDish?.let { dish ->
                            SpinTargetPrizeCard(dish = dish)
                        }

                        // Big 3D Tactile SPIN CTA Button
                        SpinActionButton(
                            isSpinning = isSpinning,
                            onClick = triggerSpin
                        )

                        // Fair Play / Blessing Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ArtisticMaroonCard,
                            border = BorderStroke(1.dp, ArtisticAmberSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                DiyaLamp(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Authentic 3D Physics • Physical friction, torque & harmonic detent locks",
                                    color = ArtisticCreamSub,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                DiyaLamp(modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
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
                color = customColors.cardBg,
                border = BorderStroke(2.dp, customColors.primaryAccent),
                shadowElevation = 24.dp,
                modifier = Modifier.shadow(24.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .background(customColors.cardBg)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = "Celebration",
                        tint = customColors.primaryAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "🎉 JACKPOT WINNER! 🎉",
                        color = customColors.primaryAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (userName.isNotBlank()) "Congratulations, $userName!" else "Congratulations!",
                        color = customColors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You won 1 Free ${selectedDish?.title ?: "Festive Delicacy"}!",
                        color = ArtisticCream,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
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

@Composable
private fun SpinPhysicsStatusPill(
    physicsState: WheelPhysicsState,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when (physicsState.phase) {
            WheelSpinPhase.IDLE -> ArtisticMaroonDark
            WheelSpinPhase.ACCELERATION -> Color(0xFF4A1A00)
            WheelSpinPhase.DECELERATION -> Color(0xFF3B1010)
            WheelSpinPhase.FINAL_LANDING -> Color(0xFF5A3000)
            WheelSpinPhase.LANDED -> if (physicsState.isLandedPrizeWin) Color(0xFF422800) else ArtisticMaroonDark
        },
        border = BorderStroke(
            1.2.dp,
            when (physicsState.phase) {
                WheelSpinPhase.IDLE -> ArtisticAmberSubtle
                WheelSpinPhase.ACCELERATION -> Color(0xFFFFB300)
                WheelSpinPhase.DECELERATION -> ArtisticAmberGold
                WheelSpinPhase.FINAL_LANDING -> Color(0xFFFFD54F)
                WheelSpinPhase.LANDED -> if (physicsState.isLandedPrizeWin) ArtisticAmberGold else ArtisticAmberSubtle
            }
        ),
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .testTag("physics_phase_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (physicsState.phase) {
                    WheelSpinPhase.IDLE -> Icons.Default.Casino
                    WheelSpinPhase.ACCELERATION -> Icons.Default.PlayArrow
                    WheelSpinPhase.DECELERATION -> Icons.Default.Refresh
                    WheelSpinPhase.FINAL_LANDING -> Icons.Default.Star
                    WheelSpinPhase.LANDED -> if (physicsState.isLandedPrizeWin) Icons.Default.Celebration else Icons.Default.Casino
                },
                contentDescription = "Physics Status",
                tint = when (physicsState.phase) {
                    WheelSpinPhase.IDLE -> ArtisticCreamSub
                    WheelSpinPhase.ACCELERATION -> Color(0xFFFFB300)
                    WheelSpinPhase.DECELERATION -> ArtisticAmberGold
                    WheelSpinPhase.FINAL_LANDING -> Color(0xFFFFD54F)
                    WheelSpinPhase.LANDED -> if (physicsState.isLandedPrizeWin) Color(0xFFFFD54F) else ArtisticCreamSub
                },
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = when (physicsState.phase) {
                    WheelSpinPhase.IDLE -> "READY TO SPIN • 3D PHYSICS"
                    WheelSpinPhase.ACCELERATION -> "TORQUE BUILDUP • ACCELERATING"
                    WheelSpinPhase.DECELERATION -> "VISCOUS FRICTION • ${(physicsState.angularVelocityDegPerSec).toInt()}°/s"
                    WheelSpinPhase.FINAL_LANDING -> "HARMONIC DETENT • LOCKING PRIZE..."
                    WheelSpinPhase.LANDED -> if (physicsState.isLandedPrizeWin) "JACKPOT LANDED • ${physicsState.landedSector?.primaryLabel ?: "WINNER!"}" else "SPIN COMPLETED • ${physicsState.landedSector?.primaryLabel ?: "TRY AGAIN"}"
                },
                color = when (physicsState.phase) {
                    WheelSpinPhase.IDLE -> ArtisticCreamSub
                    WheelSpinPhase.ACCELERATION -> Color(0xFFFFE082)
                    WheelSpinPhase.DECELERATION -> ArtisticAmberGold
                    WheelSpinPhase.FINAL_LANDING -> Color(0xFFFFF9C4)
                    WheelSpinPhase.LANDED -> if (physicsState.isLandedPrizeWin) Color(0xFFFFF9C4) else ArtisticCream
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun SpinTargetPrizeCard(
    dish: Dish,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ArtisticMaroonCard,
        border = BorderStroke(1.2.dp, ArtisticAmberGold),
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DishIllustration(
                dish = dish,
                modifier = Modifier.size(68.dp)
            )

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

@Composable
private fun SpinActionButton(
    isSpinning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = AppTheme.customColors
    Button(
        onClick = onClick,
        enabled = !isSpinning,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(if (!isSpinning) 12.dp else 0.dp, RoundedCornerShape(18.dp))
            .testTag("spin_now_button"),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = customColors.primaryAccent,
            contentColor = customColors.textOnAccent,
            disabledContainerColor = customColors.primaryAccent.copy(alpha = 0.4f),
            disabledContentColor = customColors.textOnAccent.copy(alpha = 0.6f)
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
                tint = customColors.textOnAccent,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isSpinning) "SPINNING 3D WHEEL..." else "SPIN THE 3D WHEEL NOW",
                color = customColors.textOnAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}
