package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Dish
import com.example.model.OrderItem
import com.example.model.QualityOption
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
import com.example.ui.theme.GreenSuccess
import kotlin.math.sin

/**
 * Material 3 Festive QR Code component for UPI instant payment at festival counters.
 */
@Composable
fun PaymentQrCodeCard(
    dish: Dish? = null,
    orderItems: List<OrderItem> = emptyList(),
    quantity: Int = 1,
    payableAmount: Int,
    isPaid: Boolean,
    isFreeItem: Boolean = false,
    qualityOption: QualityOption? = null,
    onMarkAsPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customColors = AppTheme.customColors
    val upiId = "ganeshutsav.fest@upi"
    val effectiveDish = dish ?: orderItems.firstOrNull()?.dish ?: Dish.KHANDVI

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(22.dp))
            .testTag("payment_qr_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = customColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.5.dp, if (isPaid) GreenSuccess else customColors.primaryAccent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: QR Payment Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Payment",
                        tint = customColors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFreeItem && payableAmount == 0) "PAYMENT & COUNTER PASS" else "SCAN QR FOR PAYMENT",
                        color = customColors.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid || (isFreeItem && payableAmount == 0)) GreenSuccess.copy(alpha = 0.15f) else customColors.surfaceDark,
                    border = BorderStroke(1.dp, if (isPaid || (isFreeItem && payableAmount == 0)) GreenSuccess else customColors.primaryAccent)
                ) {
                    Text(
                        text = if (isPaid) "✓ PAID VIA QR" else if (isFreeItem && payableAmount == 0) "FREE WIN" else "PENDING",
                        color = if (isPaid || (isFreeItem && payableAmount == 0)) GreenSuccess else customColors.primaryAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Bill Breakdown Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = customColors.surfaceDark,
                border = BorderStroke(1.dp, customColors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (orderItems.isNotEmpty()) {
                        orderItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.dish.title} (${item.quantity}x)",
                                    color = customColors.textPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${item.totalPrice}",
                                    color = customColors.textSecondary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (isFreeItem) {
                            val freeItemLabel = if (dish != null) "🎁 Free 1x ${dish.title} Prize applied!" else "🎁 Free 1x Prize applied!"
                            Text(
                                text = freeItemLabel,
                                color = GreenSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${effectiveDish.title} ($quantity items)",
                                color = customColors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payable",
                            color = customColors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (payableAmount == 0) "₹0 (FREE)" else "₹$payableAmount",
                            color = if (payableAmount == 0) GreenSuccess else customColors.primaryAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Stylized Authentic High-Contrast QR Pattern Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(2.5.dp, customColors.primaryAccent),
                modifier = Modifier
                    .size(190.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .testTag("qr_code_graphic")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FestiveQrPattern(
                        amount = payableAmount,
                        dishTag = effectiveDish.name
                    )

                    // Center G-Utsav Logo Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = customColors.cardBg,
                        border = BorderStroke(1.5.dp, customColors.primaryAccent),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🕉️",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // UPI ID & Scan Note
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = customColors.surfaceDark,
                border = BorderStroke(1.dp, customColors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "UPI ID: $upiId",
                            color = customColors.primaryAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Accepted on GPay, PhonePe, Paytm, BHIM",
                            color = customColors.textSecondary,
                            fontSize = 9.5.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", upiId))
                            Toast.makeText(context, "UPI ID copied: $upiId", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy UPI ID",
                            tint = customColors.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Mark As Paid / Paid Status Button
            if (!isPaid && payableAmount > 0) {
                Button(
                    onClick = onMarkAsPaid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                        .testTag("btn_mark_as_paid"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.primaryAccent,
                        contentColor = customColors.textOnAccent
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirm Payment",
                            tint = customColors.textOnAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PAID ₹$payableAmount VIA QR (CONFIRM)",
                            color = customColors.textOnAccent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else if (isPaid) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GreenSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = GreenSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payment of ₹$payableAmount Confirmed & Logged!",
                            color = GreenSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Procedural Vector QR Code pattern drawing standard QR finder patterns and modules.
 */
@Composable
private fun FestiveQrPattern(
    amount: Int,
    dishTag: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val gridSize = 21
        val cellSize = size.width / gridSize
        val blackColor = Color(0xFF160202)

        // Draw background white
        drawRect(Color.White, size = size)

        fun drawFinderPattern(startX: Int, startY: Int) {
            // Outer 7x7
            drawRoundRect(
                color = blackColor,
                topLeft = Offset(startX * cellSize, startY * cellSize),
                size = Size(7 * cellSize, 7 * cellSize),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            // Inner 5x5 white
            drawRoundRect(
                color = Color.White,
                topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                size = Size(5 * cellSize, 5 * cellSize),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            // Center 3x3 black
            drawRoundRect(
                color = blackColor,
                topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                size = Size(3 * cellSize, 3 * cellSize),
                cornerRadius = CornerRadius(1.dp.toPx())
            )
        }

        // 3 Standard QR Finder Eyes
        drawFinderPattern(0, 0)
        drawFinderPattern(gridSize - 7, 0)
        drawFinderPattern(0, gridSize - 7)

        // Timing patterns
        for (i in 7 until gridSize - 7) {
            if (i % 2 == 0) {
                drawRect(
                    color = blackColor,
                    topLeft = Offset(6 * cellSize, i * cellSize),
                    size = Size(cellSize, cellSize)
                )
                drawRect(
                    color = blackColor,
                    topLeft = Offset(i * cellSize, 6 * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }
        }

        // Deterministic procedural data modules
        val seed = amount * 31 + dishTag.hashCode()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                // Avoid finder pattern zones
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= gridSize - 8
                val inBottomLeft = r >= gridSize - 8 && c < 8
                val inCenterLogo = r in 8..12 && c in 8..12

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenterLogo) {
                    val hash = (r * 37 + c * 17 + seed + (sin((r * c).toDouble()) * 1000).toInt()) % 100
                    if (hash % 2 == 0) {
                        drawRoundRect(
                            color = blackColor,
                            topLeft = Offset(c * cellSize + 0.5f, r * cellSize + 0.5f),
                            size = Size(cellSize - 1f, cellSize - 1f),
                            cornerRadius = CornerRadius(1.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
