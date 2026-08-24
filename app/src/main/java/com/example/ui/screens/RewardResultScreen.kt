package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LocalFestiveSoundManager
import com.example.model.Dish
import com.example.model.SpinResult
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.DishIllustration
import com.example.ui.components.DiyaLamp
import com.example.ui.components.MarigoldGarland
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

@Composable
fun RewardResultScreen(
    result: SpinResult?,
    selectedDish: Dish?,
    onClaimAndReset: () -> Unit,
    onSpinAgain: () -> Unit,
    onRestart: () -> Unit,
    onOpenHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val soundManager = LocalFestiveSoundManager.current
    val isWin = result?.isWin == true
    val userName = result?.userName ?: "Valued Guest"
    val wonDish = result?.wonDish ?: selectedDish

    LaunchedEffect(result) {
        if (result != null) {
            if (isWin) {
                soundManager?.playWinChime()
            } else {
                soundManager?.playTryAgainSound()
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
            MarigoldGarland(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isWin) {
                    WinContent(
                        userName = userName,
                        dish = wonDish,
                        claimCode = result?.claimCode ?: "LUCKY-WIN",
                        onClaimAndReset = onClaimAndReset,
                        onOpenHistory = onOpenHistory
                    )
                } else {
                    TryAgainContent(
                        userName = userName,
                        dish = selectedDish,
                        onSpinAgain = onSpinAgain,
                        onRestart = onRestart,
                        onOpenHistory = onOpenHistory
                    )
                }
            }
        }

        // Win celebration confetti shower
        if (isWin) {
            ConfettiOverlay()
        }
    }
}

@Composable
private fun WinContent(
    userName: String,
    dish: Dish?,
    claimCode: String,
    onClaimAndReset: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.9f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(26.dp))
                .testTag("win_result_card"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = ArtisticMaroonCard.copy(alpha = 0.98f)
            ),
            border = BorderStroke(2.dp, ArtisticAmberGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Celebration Header with Diyas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiyaLamp(modifier = Modifier.size(38.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🏆 FESTIVE WINNER! 🏆",
                            color = ArtisticAmberGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Congratulations!",
                            color = ArtisticAmberGold,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DiyaLamp(modifier = Modifier.size(38.dp))
                }

                // Won Dish Graphic Art Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    dish?.let {
                        DishIllustration(
                            dish = it,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Dish Title Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ArtisticMaroonSurface,
                    border = BorderStroke(1.2.dp, ArtisticAmberGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1x FREE ${dish?.title?.uppercase() ?: "DELICACY"}",
                            color = ArtisticAmberGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${dish?.nativeTitle} • Freshly Prepared Festival Special",
                            color = ArtisticCream,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Greeting & Claim Instructions
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ArtisticMaroonDark,
                    border = BorderStroke(0.8.dp, ArtisticAmberSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Namaste, $userName!",
                            color = ArtisticAmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You won 1 Free ${dish?.title ?: "Dish"}! Show this screen at the festival food counter to claim.",
                            color = ArtisticCreamSub,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Official Voucher Claim Ticket
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ArtisticMaroonSurface,
                    border = BorderStroke(1.5.dp, ArtisticAmberGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFICIAL CLAIM PASS",
                                color = ArtisticAmberGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = "Ticket",
                                tint = ArtisticAmberGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Voucher Code Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArtisticMaroonDark,
                            border = BorderStroke(1.dp, ArtisticAmberGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "VOUCHER CODE",
                                    color = ArtisticCreamSub,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = claimCode,
                                    color = ArtisticAmberGold,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 3.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Valid for single redemption today at the festival counter.",
                            color = ArtisticCreamSub.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Action Buttons: Claim & Reset + View History
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onClaimAndReset,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .testTag("claim_reset_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArtisticAmberGold,
                            contentColor = ArtisticMaroonBg
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Claim",
                                tint = ArtisticMaroonBg,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLAIM & RESET",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            soundManager?.playClickSound()
                            onOpenHistory()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("view_history_from_win_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, ArtisticAmberGold),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ArtisticAmberGold,
                            containerColor = ArtisticMaroonDark
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "View Winnings & History",
                                tint = ArtisticAmberGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View All Winnings & History",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisticAmberGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TryAgainContent(
    userName: String,
    dish: Dish?,
    onSpinAgain: () -> Unit,
    onRestart: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(26.dp))
            .testTag("try_again_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = ArtisticMaroonCard.copy(alpha = 0.98f)
        ),
        border = BorderStroke(1.5.dp, FestiveCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Diya & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DiyaLamp(modifier = Modifier.size(36.dp))
                Text(
                    text = "BETTER LUCK NEXT TIME",
                    color = ArtisticAmberGlow,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                DiyaLamp(modifier = Modifier.size(36.dp))
            }

            // Friendly Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ArtisticMaroonDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentDissatisfied,
                    contentDescription = "Try again icon",
                    tint = ArtisticAmberGold,
                    modifier = Modifier.size(38.dp)
                )
            }

            Text(
                text = "Almost Had It, $userName!",
                color = ArtisticCream,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "The 3D wheel just missed the prize sector! Don't worry—the wheel is still hot and waiting for you to win your free ${dish?.title ?: "delicacy"}.",
                color = ArtisticCreamSub,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )

            // Target Dish Reminder Banner
            dish?.let {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ArtisticMaroonDark,
                    border = BorderStroke(1.dp, ArtisticAmberSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = it.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Target: Free ${it.title} (${it.nativeTitle})",
                            color = ArtisticAmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // CTA Buttons: Spin Again & Start Over
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSpinAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .testTag("spin_again_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArtisticAmberGold,
                        contentColor = ArtisticMaroonBg
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Spin Again",
                            tint = ArtisticMaroonBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SPIN 3D WHEEL AGAIN",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        soundManager?.playClickSound()
                        onOpenHistory()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("view_history_from_reward_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, ArtisticAmberGold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ArtisticAmberGold,
                        containerColor = ArtisticMaroonDark
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View Winnings & History",
                            tint = ArtisticAmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Winnings & Spin History",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticAmberGold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("start_over_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ArtisticAmberSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ArtisticAmberGold
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Change Dish",
                            tint = ArtisticAmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change Dish / Start Over",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
