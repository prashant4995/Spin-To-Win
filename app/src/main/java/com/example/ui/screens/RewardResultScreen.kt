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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.ui.components.PaymentQrCodeCard
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
import com.example.ui.theme.GreenSuccess

@Composable
fun RewardResultScreen(
    result: SpinResult?,
    selectedDish: Dish?,
    quantity: Int = 1,
    isPaidViaQr: Boolean = false,
    onMarkPaidViaQr: (Int) -> Unit = {},
    onClaimAndReset: () -> Unit,
    onSpinAgain: () -> Unit = {},
    onRestart: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val soundManager = LocalFestiveSoundManager.current
    val userName = result?.userName ?: "Valued Guest"
    val wonDish = result?.wonDish ?: selectedDish ?: Dish.MODAK
    val finalQuantity = result?.quantity ?: quantity

    LaunchedEffect(result) {
        if (result != null) {
            soundManager?.playWinChime()
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                WinContent(
                    userName = userName,
                    dish = wonDish,
                    quantity = finalQuantity,
                    isPaidViaQr = isPaidViaQr || result?.isPaidViaQr == true,
                    onMarkPaidViaQr = onMarkPaidViaQr,
                    onClaimAndReset = onClaimAndReset,
                    onOpenHistory = onOpenHistory
                )
            }
        }

        // Win celebration confetti shower
        ConfettiOverlay(trigger = result)
    }
}

@Composable
private fun WinContent(
    userName: String,
    dish: Dish,
    quantity: Int,
    isPaidViaQr: Boolean,
    onMarkPaidViaQr: (Int) -> Unit,
    onClaimAndReset: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current

    // In a win, 1 item is free. Any extra quantity (> 1) is payable.
    val extraItems = (quantity - 1).coerceAtLeast(0)
    val payableAmount = extraItems * dish.pricePerUnit

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
                    .padding(18.dp)
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
                    DiyaLamp(modifier = Modifier.size(36.dp))
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DiyaLamp(modifier = Modifier.size(36.dp))
                }

                // Won Dish Graphic Art Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    DishIllustration(
                        dish = dish,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dish Title & Win Banner
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
                            text = "1x FREE ${dish.title.uppercase()} (WORTH ₹${dish.pricePerUnit})",
                            color = ArtisticAmberGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${dish.nativeTitle} • Authentic Festival Special Prasad",
                            color = ArtisticCream,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Guest Greeting Box
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
                            text = if (quantity == 1) {
                                "You won 1 Free ${dish.title}! Show this screen at the counter to claim your hot prasad."
                            } else {
                                "You ordered $quantity items: 1 is 100% FREE as your prize, and remaining $extraItems items are ₹$payableAmount total."
                            },
                            color = ArtisticCreamSub,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }


                // QR CODE FOR PAYMENT & COUNTER PASS (Replaces Voucher Code)
                PaymentQrCodeCard(
                    dish = dish,
                    quantity = quantity,
                    payableAmount = payableAmount,
                    isPaid = isPaidViaQr,
                    isFreeItem = true,
                    onMarkAsPaid = {
                        soundManager?.playClaimChime()
                        onMarkPaidViaQr(payableAmount)
                    }
                )

                // Action Buttons: Claim & Reset + View History
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            soundManager?.playClaimChime()
                            onClaimAndReset()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .testTag("done_button"),
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
                                contentDescription = "Done",
                                tint = ArtisticMaroonBg,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DONE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
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
                                text = "View Orders, Sales & History",
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
    dish: Dish,
    quantity: Int,
    isPaidViaQr: Boolean,
    onMarkPaidViaQr: (Int) -> Unit,
    onSpinAgain: () -> Unit,
    onClaimAndReset: () -> Unit,
    onRestart: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current
    val totalAmount = quantity * dish.pricePerUnit

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
                .padding(18.dp)
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
                DiyaLamp(modifier = Modifier.size(34.dp))
                Text(
                    text = "BETTER LUCK NEXT TIME",
                    color = ArtisticAmberGlow,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                DiyaLamp(modifier = Modifier.size(34.dp))
            }

            // Friendly Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(ArtisticMaroonDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentDissatisfied,
                    contentDescription = "Try again icon",
                    tint = ArtisticAmberGold,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = "Almost Had It, $userName!",
                color = ArtisticCream,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You can spin again or complete payment below via QR to enjoy fresh $quantity plates of ${dish.title} (₹$totalAmount).",
                color = ArtisticCreamSub,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )

            // QR CODE FOR PAYMENT DIRECTLY AFTER RESULT (Requirement 5)
            PaymentQrCodeCard(
                dish = dish,
                quantity = quantity,
                payableAmount = totalAmount,
                isPaid = isPaidViaQr,
                isFreeItem = false,
                onMarkAsPaid = {
                    soundManager?.playClaimChime()
                    onMarkPaidViaQr(totalAmount)
                }
            )

            // CTA Buttons: Done / Spin Again & Start Over & View History
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        soundManager?.playClaimChime()
                        onClaimAndReset()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .testTag("done_try_again_button"),
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
                            contentDescription = "Done",
                            tint = ArtisticMaroonBg,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DONE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSpinAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("spin_again_button"),
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
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Spin Again",
                            tint = ArtisticAmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SPIN 3D WHEEL AGAIN",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
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
                        .height(44.dp)
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
                            text = "View Orders, Sales & History",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticAmberGold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
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
                            text = "Change Dish / New Customer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
