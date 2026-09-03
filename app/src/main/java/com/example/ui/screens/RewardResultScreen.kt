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
import com.example.model.OrderItem
import com.example.model.QualityOption
import com.example.model.SpinResult
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.DishIllustration
import com.example.ui.components.DiyaLamp
import com.example.ui.components.MarigoldGarland
import com.example.ui.components.PaymentQrCodeCard
import com.example.ui.theme.AppTheme
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
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors
    val userName = result?.userName ?: "Valued Guest"
    val wonDish = result?.wonDish ?: selectedDish ?: Dish.MODAK
    val finalQuantity = result?.quantity ?: quantity
    val orderItems = result?.items ?: emptyList()

    LaunchedEffect(result) {
        if (result != null) {
            if (result.isWin) {
                soundManager?.playCelebrationSound()
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
                        customColors.bg,
                        customColors.bgSurface,
                        customColors.cardBgSubtle
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
                if (result?.isWin == true) {
                    WinContent(
                        userName = userName,
                        dish = wonDish,
                        orderItems = orderItems,
                        quantity = finalQuantity,
                        isPaidViaQr = isPaidViaQr || result?.isPaidViaQr == true,
                        onMarkPaidViaQr = onMarkPaidViaQr,
                        onClaimAndReset = onClaimAndReset,
                        onOpenHistory = onOpenHistory
                    )
                } else {
                    TryAgainContent(
                        userName = userName,
                        dish = wonDish,
                        orderItems = orderItems,
                        quantity = finalQuantity,
                        isPaidViaQr = isPaidViaQr || result?.isPaidViaQr == true,
                        isDirectCheckout = result?.isDirectCheckout == true,
                        onMarkPaidViaQr = onMarkPaidViaQr,
                        onSpinAgain = onSpinAgain,
                        onClaimAndReset = onClaimAndReset,
                        onRestart = onRestart,
                        onOpenHistory = onOpenHistory
                    )
                }
            }
        }

        // Win celebration confetti shower (only on win)
        if (result?.isWin == true) {
            ConfettiOverlay(trigger = result)
        }
    }
}

@Composable
private fun WinContent(
    userName: String,
    dish: Dish,
    orderItems: List<OrderItem>,
    quantity: Int,
    isPaidViaQr: Boolean,
    onMarkPaidViaQr: (Int) -> Unit,
    onClaimAndReset: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors

    val totalOrderAmt = if (orderItems.isNotEmpty()) {
        orderItems.sumOf { it.totalPrice }
    } else {
        quantity * dish.pricePerUnit
    }
    val isWonDishInOrder = orderItems.isEmpty() || orderItems.any { it.dish == dish }
    val discount = if (isWonDishInOrder) dish.pricePerUnit else 0
    val payableAmount = (totalOrderAmt - discount).coerceAtLeast(0)

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
                containerColor = customColors.cardBg
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),
            border = BorderStroke(2.dp, customColors.primaryAccent)
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
                            color = customColors.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Congratulations!",
                            color = customColors.primaryAccent,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DiyaLamp(modifier = Modifier.size(36.dp))
                }

                // Won Dish Graphic Art Box
                DishIllustration(
                    dish = dish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // Dish Title & Win Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = customColors.surfaceDark,
                    border = BorderStroke(1.2.dp, customColors.primaryAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1x FREE ${dish.title.uppercase()} (WORTH ₹${dish.pricePerUnit})",
                            color = customColors.primaryAccent,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 0.8.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${dish.nativeTitle} • Authentic Special Prasad Prize",
                            color = customColors.textSecondary,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Guest Greeting Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = customColors.surfaceDark,
                    border = BorderStroke(1.dp, customColors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Namaste, $userName!",
                            color = customColors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                payableAmount == 0 ->
                                    "You won 1 Free ${dish.title}! Show this screen at the counter to claim your hot prasad."
                                !isWonDishInOrder ->
                                    "You ordered $quantity items and won 1 Free ${dish.title}! Show this screen at the counter to claim your free ${dish.title} prasad. Total order payable is ₹$payableAmount."
                                else ->
                                    "You ordered $quantity items: 1x ${dish.title} is 100% FREE as your prize, remaining total is ₹$payableAmount."
                            },
                            color = customColors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // QR CODE FOR PAYMENT & COUNTER PASS
                PaymentQrCodeCard(
                    dish = dish,
                    orderItems = orderItems,
                    quantity = quantity,
                    payableAmount = payableAmount,
                    isPaid = isPaidViaQr,
                    isFreeItem = true,
                    onMarkAsPaid = {
                        soundManager?.playClaimChime()
                        onMarkPaidViaQr(payableAmount)
                    }
                )

                // Action Buttons: Done / Complete & View History
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
                            containerColor = customColors.primaryAccent,
                            contentColor = customColors.textOnAccent
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
                                tint = customColors.textOnAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DONE",
                                color = customColors.textOnAccent,
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
                        border = BorderStroke(1.2.dp, customColors.primaryAccent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = customColors.primaryAccent,
                            containerColor = customColors.surfaceDark
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "View Winnings & History",
                                tint = customColors.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Orders, Sales & History",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = customColors.primaryAccent
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
    orderItems: List<OrderItem>,
    quantity: Int,
    isPaidViaQr: Boolean,
    isDirectCheckout: Boolean = false,
    onMarkPaidViaQr: (Int) -> Unit,
    onSpinAgain: () -> Unit,
    onClaimAndReset: () -> Unit,
    onRestart: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors
    val totalAmount = if (orderItems.isNotEmpty()) {
        orderItems.sumOf { it.totalPrice }
    } else {
        quantity * dish.pricePerUnit
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .testTag("try_again_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = customColors.cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        border = BorderStroke(1.5.dp, customColors.cardBorder)
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
                    text = if (isDirectCheckout) "ORDER CHECKOUT & PAYMENT" else "BETTER LUCK NEXT TIME",
                    color = customColors.primaryAccent,
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
                    .background(customColors.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDirectCheckout) Icons.Default.ShoppingBag else Icons.Default.SentimentDissatisfied,
                    contentDescription = if (isDirectCheckout) "Checkout icon" else "Try again icon",
                    tint = customColors.primaryAccent,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = if (isDirectCheckout) "Order Summary for $userName" else "Almost Had It, $userName!",
                color = customColors.textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isDirectCheckout) {
                    "Scan QR code below to complete payment for $quantity item${if (quantity > 1) "s" else ""} (₹$totalAmount total)."
                } else {
                    "Complete payment below via QR to enjoy fresh $quantity item${if (quantity > 1) "s" else ""} (₹$totalAmount total)."
                },
                color = customColors.textSecondary,
                fontSize = 12.5.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )

            // QR CODE FOR PAYMENT DIRECTLY AFTER RESULT
            PaymentQrCodeCard(
                dish = dish,
                orderItems = orderItems,
                quantity = quantity,
                payableAmount = totalAmount,
                isPaid = isPaidViaQr,
                isFreeItem = false,
                onMarkAsPaid = {
                    soundManager?.playClaimChime()
                    onMarkPaidViaQr(totalAmount)
                }
            )

            // CTA Buttons: Done / Complete Order & View History & Start Over
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
                        containerColor = customColors.primaryAccent,
                        contentColor = customColors.textOnAccent
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
                            tint = customColors.textOnAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DONE",
                            color = customColors.textOnAccent,
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
                        .height(44.dp)
                        .testTag("view_history_from_reward_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, customColors.primaryAccent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = customColors.primaryAccent,
                        containerColor = customColors.surfaceDark
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View Winnings & History",
                            tint = customColors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Orders, Sales & History",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = customColors.primaryAccent
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
                    border = BorderStroke(1.dp, customColors.cardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = customColors.primaryAccent,
                        containerColor = customColors.cardBg
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Change Dish",
                            tint = customColors.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Customer / Select Delicacies",
                            color = customColors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
