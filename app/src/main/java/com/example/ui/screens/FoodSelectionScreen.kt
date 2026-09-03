package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.LocalFestiveSoundManager
import com.example.model.Dish
import com.example.model.OrderItem
import com.example.ui.components.DishIllustration
import com.example.ui.components.DiyaLamp
import com.example.ui.components.GaneshaIdolIcon
import com.example.ui.components.MarigoldGarland
import com.example.ui.theme.AppTheme
import com.example.ui.theme.GreenSuccess

@Composable
fun FoodSelectionScreen(
    userName: String,
    nameError: String?,
    dishQuantities: Map<Dish, Int>,
    totalQuantity: Int,
    totalOrderAmount: Int,
    canProceed: Boolean,
    onNameChanged: (String) -> Unit,
    onDishQuantityChanged: (Dish, Int) -> Unit,
    onIncrementDishQuantity: (Dish) -> Unit,
    onDecrementDishQuantity: (Dish) -> Unit,
    onProceedClicked: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors

    val activeItems = Dish.entries.mapNotNull { dish ->
        val qty = dishQuantities[dish] ?: 0
        if (qty > 0) OrderItem(dish, qty) else null
    }

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
        val isTablet = maxWidth >= 720.dp
        val leftScrollState = rememberScrollState()
        val rightScrollState = rememberScrollState()

        val isOnlyFestivalCombos = (dishQuantities[Dish.COMBO_PLATE] ?: 0) > 0 &&
                dishQuantities.filterKeys { it != Dish.COMBO_PLATE }.values.all { it == 0 }
        val isLuckySpinUnlocked = totalQuantity > 2 && !isOnlyFestivalCombos

        // Soft Ambient Accent Aura in Background
        Box(
            modifier = Modifier
                .size(if (isTablet) 480.dp else 340.dp)
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            customColors.accentGlow.copy(alpha = if (customColors.isDark) 0.12f else 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Marigold Garland
            MarigoldGarland(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            if (isTablet) {
                // TABLET / EXPANDED 2-PANE RESPONSIVE LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    HeaderBannerCard(
                        userName = userName,
                        onOpenSettings = onOpenSettings,
                        onOpenHistory = onOpenHistory
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Column: Customer Registration, Live Order Summary, Eligibility & Checkout CTA
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(leftScrollState),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CustomerNameInputCard(
                                userName = userName,
                                nameError = nameError,
                                onNameChanged = onNameChanged
                            )

                            OrderTotalSummaryCard(
                                activeItems = activeItems,
                                totalQuantity = totalQuantity,
                                totalOrderAmount = totalOrderAmount
                            )

                            EligibilityPill(
                                isOnlyFestivalCombos = isOnlyFestivalCombos,
                                isLuckySpinUnlocked = isLuckySpinUnlocked,
                                totalQuantity = totalQuantity
                            )

                            ProceedButton(
                                onProceedClicked = onProceedClicked,
                                totalQuantity = totalQuantity,
                                isLuckySpinUnlocked = isLuckySpinUnlocked,
                                totalOrderAmount = totalOrderAmount
                            )

                            FestiveBlessingFooter()
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Right Column: Delicacies Selection Menu
                        Column(
                            modifier = Modifier
                                .weight(1.15f)
                                .fillMaxHeight()
                                .verticalScroll(rightScrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DelicacyCardsList(
                                dishQuantities = dishQuantities,
                                totalQuantity = totalQuantity,
                                onDishQuantityChanged = onDishQuantityChanged,
                                onIncrementDishQuantity = onIncrementDishQuantity,
                                onDecrementDishQuantity = onDecrementDishQuantity
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            } else {
                // PHONE / COMPACT SINGLE-COLUMN LAYOUT (Centered with width constraint)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .widthIn(max = 600.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderBannerCard(
                        userName = userName,
                        onOpenSettings = onOpenSettings,
                        onOpenHistory = onOpenHistory
                    )

                    CustomerNameInputCard(
                        userName = userName,
                        nameError = nameError,
                        onNameChanged = onNameChanged
                    )

                    DelicacyCardsList(
                        dishQuantities = dishQuantities,
                        totalQuantity = totalQuantity,
                        onDishQuantityChanged = onDishQuantityChanged,
                        onIncrementDishQuantity = onIncrementDishQuantity,
                        onDecrementDishQuantity = onDecrementDishQuantity
                    )

                    OrderTotalSummaryCard(
                        activeItems = activeItems,
                        totalQuantity = totalQuantity,
                        totalOrderAmount = totalOrderAmount
                    )

                    EligibilityPill(
                        isOnlyFestivalCombos = isOnlyFestivalCombos,
                        isLuckySpinUnlocked = isLuckySpinUnlocked,
                        totalQuantity = totalQuantity
                    )

                    ProceedButton(
                        onProceedClicked = onProceedClicked,
                        totalQuantity = totalQuantity,
                        isLuckySpinUnlocked = isLuckySpinUnlocked,
                        totalOrderAmount = totalOrderAmount
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderBannerCard(
    userName: String,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors
    val isMuted by (soundManager?.isMuted ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsStateWithLifecycle(false)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = customColors.cardBg,
        border = BorderStroke(1.2.dp, customColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { soundManager?.toggleMute() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(customColors.surfaceDark)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute festive sounds" else "Mute festive sounds",
                        tint = if (isMuted) customColors.textSecondary else customColors.primaryAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = {
                        soundManager?.playClickSound()
                        onOpenSettings()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(customColors.surfaceDark)
                        .testTag("theme_settings_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme & Colors",
                        tint = customColors.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GaneshaIdolIcon(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "|| श्री गणेशाय नमः ||",
                        color = customColors.primaryAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GaneshaIdolIcon(modifier = Modifier.size(24.dp))
                }
                Text(
                    text = if (userName.isNotBlank()) "Namaste, $userName!" else "Namaste & Welcome!",
                    color = customColors.primaryAccent,
                    fontSize = 19.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager?.playClickSound()
                        onOpenHistory()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(customColors.surfaceDark)
                        .testTag("history_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "View History & Winnings",
                        tint = customColors.primaryAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }
                DiyaLamp(modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun CustomerNameInputCard(
    userName: String,
    nameError: String?,
    onNameChanged: (String) -> Unit
) {
    val customColors = AppTheme.customColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = customColors.cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        border = BorderStroke(1.2.dp, customColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Enter Your Name",
                    color = customColors.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )

                // Monogram Avatar Badge
                val initialLetter = if (userName.isNotBlank()) userName.first().uppercase() else "✦"
                Surface(
                    shape = CircleShape,
                    color = customColors.primaryAccent,
                    border = BorderStroke(2.dp, customColors.cardBorder),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initialLetter,
                            color = customColors.textOnAccent,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_input_field"),
                placeholder = {
                    Text(
                        text = "e.g., Ananya Sharma",
                        color = customColors.textSecondary.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Name",
                        tint = customColors.primaryAccent
                    )
                },
                trailingIcon = {
                    if (userName.isNotEmpty()) {
                        IconButton(onClick = { onNameChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Name",
                                tint = customColors.textSecondary
                            )
                        }
                    }
                },
                isError = nameError != null,
                supportingText = if (nameError != null) {
                    { Text(text = nameError, color = Color(0xFFFF5252), fontSize = 11.sp) }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = customColors.textPrimary,
                    unfocusedTextColor = customColors.textPrimary,
                    focusedBorderColor = customColors.primaryAccent,
                    unfocusedBorderColor = customColors.cardBorder,
                    focusedContainerColor = customColors.surfaceDark,
                    unfocusedContainerColor = customColors.surfaceDark,
                    cursorColor = customColors.primaryAccent
                )
            )
        }
    }
}

@Composable
private fun DelicacyCardsList(
    dishQuantities: Map<Dish, Int>,
    totalQuantity: Int,
    onDishQuantityChanged: (Dish, Int) -> Unit,
    onIncrementDishQuantity: (Dish) -> Unit,
    onDecrementDishQuantity: (Dish) -> Unit
) {
    val soundManager = LocalFestiveSoundManager.current
    val customColors = AppTheme.customColors

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Select Delicacies & Quantities",
                    color = customColors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Select multiple delicacies in a single order below",
                    color = customColors.primaryAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (totalQuantity > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = customColors.primaryAccent,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "🛒 $totalQuantity Items",
                        color = customColors.textOnAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Individual Delicacy Cards with Stepper & Presets
        Dish.entries.forEach { dish ->
            val qty = dishQuantities[dish] ?: 0
            MultiDishCard(
                dish = dish,
                quantity = qty,
                onQuantityChanged = { newQty ->
                    soundManager?.playClickSound()
                    onDishQuantityChanged(dish, newQty)
                },
                onIncrement = {
                    soundManager?.playClickSound()
                    onIncrementDishQuantity(dish)
                },
                onDecrement = {
                    soundManager?.playClickSound()
                    onDecrementDishQuantity(dish)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_${dish.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun OrderTotalSummaryCard(
    activeItems: List<OrderItem>,
    totalQuantity: Int,
    totalOrderAmount: Int
) {
    val customColors = AppTheme.customColors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .testTag("total_summary_card"),
        shape = RoundedCornerShape(20.dp),
        color = customColors.cardBg,
        border = BorderStroke(1.5.dp, customColors.primaryAccent.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Total",
                        tint = customColors.primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Order Total",
                        color = customColors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = customColors.surfaceDark,
                    border = BorderStroke(1.dp, customColors.primaryAccent)
                ) {
                    Text(
                        text = if (totalQuantity > 0) "$totalQuantity Items Selected" else "0 Items",
                        color = customColors.primaryAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Calculation Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = customColors.surfaceDark,
                border = BorderStroke(1.dp, customColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeItems.isEmpty()) {
                        Text(
                            text = "No delicacies added yet. Use the quantity steppers above to add items to your festive order.",
                            color = customColors.textSecondary,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        activeItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.dish.title} (${item.dish.nativeTitle})",
                                        color = customColors.textPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${item.quantity} ${item.dish.portionUnit} × ₹${item.unitPrice}",
                                        color = customColors.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "₹${item.totalPrice}",
                                    color = customColors.textPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Divider(
                            thickness = 1.dp,
                            color = customColors.cardBorder
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Payable Amount",
                                color = customColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "₹$totalOrderAmount",
                                color = customColors.primaryAccent,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EligibilityPill(
    isOnlyFestivalCombos: Boolean,
    isLuckySpinUnlocked: Boolean,
    totalQuantity: Int
) {
    val customColors = AppTheme.customColors

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = customColors.surfaceDark,
        border = BorderStroke(
            1.dp,
            if (isLuckySpinUnlocked) customColors.primaryAccent.copy(alpha = 0.5f) else customColors.cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isLuckySpinUnlocked) GreenSuccess else if (totalQuantity > 0) customColors.primaryAccent else customColors.textSecondary)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = when {
                    isOnlyFestivalCombos -> "💳 Direct Checkout (Spin & Win is not applicable for Festival Combo orders)"
                    isLuckySpinUnlocked -> "🎉 3D Lucky Spin Unlocked! ($totalQuantity items qualify for free spin prize)"
                    totalQuantity in 1..2 -> "💳 Direct Checkout (Add ${3 - totalQuantity} more delicacy to unlock 3D Lucky Spin!)"
                    else -> "👆 Set quantities above to add items to your festive order"
                },
                color = customColors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProceedButton(
    onProceedClicked: () -> Unit,
    totalQuantity: Int,
    isLuckySpinUnlocked: Boolean,
    totalOrderAmount: Int
) {
    val customColors = AppTheme.customColors

    Button(
        onClick = onProceedClicked,
        enabled = totalQuantity > 0,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(if (totalQuantity > 0) 12.dp else 0.dp, RoundedCornerShape(18.dp))
            .testTag("proceed_spin_button"),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = customColors.primaryAccent,
            contentColor = customColors.textOnAccent,
            disabledContainerColor = customColors.surfaceDark,
            disabledContentColor = customColors.textSecondary.copy(alpha = 0.5f)
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
                imageVector = if (isLuckySpinUnlocked) Icons.Default.Stars else Icons.Default.ShoppingBag,
                contentDescription = if (isLuckySpinUnlocked) "Spin" else "Pay",
                tint = if (totalQuantity > 0) customColors.textOnAccent else customColors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = when {
                    isLuckySpinUnlocked -> "PROCEED TO 3D LUCKY SPIN (₹$totalOrderAmount)"
                    totalQuantity > 0 -> "PROCEED TO PAY ₹$totalOrderAmount"
                    else -> "SELECT AT LEAST 1 DELICACY"
                },
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            )
        }
    }
}

@Composable
private fun FestiveBlessingFooter() {
    val customColors = AppTheme.customColors

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = customColors.cardBg,
        border = BorderStroke(1.dp, customColors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            DiyaLamp(modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Shuddha Ghee & Authentic Recipe • Made fresh for Ganesh Utsav",
                color = customColors.textSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            DiyaLamp(modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun MultiDishCard(
    dish: Dish,
    quantity: Int,
    onQuantityChanged: (Int) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = AppTheme.customColors
    val isSelected = quantity > 0

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) customColors.primaryAccent else customColors.cardBorder,
        animationSpec = tween(300),
        label = "border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = tween(300),
        label = "border_width"
    )

    val quantityHeaderTitle = when (dish) {
        Dish.MODAK -> "Number of Modak"
        Dish.KHANDVI -> "Number of Khandvi"
        Dish.COMBO_PLATE -> "Number of Festive Combo"
    }

    val quantityUnitLabel = when (dish) {
        Dish.MODAK -> if (quantity == 1) "Piece" else "Pieces"
        Dish.KHANDVI -> if (quantity == 1) "Plate" else "Plates"
        Dish.COMBO_PLATE -> if (quantity == 1) "Combo Plate" else "Combo Plates"
    }

    val displayUnitPrice = dish.pricePerUnit
    val cardTotalPrice = displayUnitPrice * quantity

    Card(
        modifier = modifier
            .shadow(if (isSelected) 10.dp else 4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) customColors.cardBgSubtle else customColors.cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 3.dp
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Delicacy Info Header (Illustration + Title + Pricing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (quantity == 0) {
                            onQuantityChanged(1)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish Illustration Graphic Icon
                DishIllustration(
                    dish = dish,
                    modifier = Modifier.size(74.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Dish Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dish.title,
                                color = customColors.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dish.nativeTitle,
                                color = customColors.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) customColors.primaryAccent else customColors.surfaceDark,
                                border = BorderStroke(1.dp, customColors.primaryAccent)
                            ) {
                                Text(
                                    text = "₹$displayUnitPrice",
                                    color = if (isSelected) customColors.textOnAccent else customColors.primaryAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "per ${dish.portionUnit}",
                                color = customColors.textSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Embedded "Number of <Dish>" Quantity Selector Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = customColors.surfaceDark,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) customColors.primaryAccent.copy(alpha = 0.6f) else customColors.cardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qty_section_${dish.name.lowercase()}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quantityHeaderTitle,
                            color = if (isSelected) customColors.primaryAccent else customColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Subtotal: ₹$cardTotalPrice",
                            color = if (isSelected) customColors.primaryAccent else customColors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stepper Controls Row (- Button, Count, + Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Minus Button
                        Surface(
                            onClick = onDecrement,
                            enabled = quantity > 0,
                            shape = RoundedCornerShape(10.dp),
                            color = if (quantity > 0) customColors.primaryAccent else customColors.cardBg,
                            border = BorderStroke(1.dp, customColors.cardBorder),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("btn_qty_minus_${dish.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease $quantityHeaderTitle",
                                    tint = if (quantity > 0) customColors.textOnAccent else customColors.textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Quantity Display Center Box
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$quantity",
                                color = if (isSelected) customColors.primaryAccent else customColors.textSecondary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (quantity > 0) quantityUnitLabel else "None added",
                                color = customColors.textSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Plus Button
                        Surface(
                            onClick = onIncrement,
                            shape = RoundedCornerShape(10.dp),
                            color = customColors.primaryAccent,
                            border = BorderStroke(1.dp, customColors.primaryAccent),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("btn_qty_plus_${dish.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase $quantityHeaderTitle",
                                    tint = customColors.textOnAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Count Presets Row (0, 1, 2, 3, 5, 10)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf(0, 1, 2, 3, 5, 10).forEach { preset ->
                            val isPresetActive = quantity == preset
                            Surface(
                                onClick = { onQuantityChanged(preset) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPresetActive) customColors.primaryAccent else customColors.cardBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isPresetActive) customColors.primaryAccent else customColors.cardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$preset",
                                    color = if (isPresetActive) customColors.textOnAccent else customColors.textPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Selection Strip
            Surface(
                onClick = {
                    if (quantity == 0) {
                        onQuantityChanged(1)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) customColors.primaryAccent else customColors.surfaceDark,
                border = BorderStroke(
                    1.2.dp,
                    if (isSelected) customColors.primaryAccent else customColors.primaryAccent.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelected) {
                            "✓ ADDED TO ORDER • $quantity $quantityUnitLabel • ₹$cardTotalPrice"
                        } else {
                            "+ ADD ${dish.title.uppercase()} (₹$displayUnitPrice/item)"
                        },
                        color = if (isSelected) customColors.textOnAccent else customColors.primaryAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
