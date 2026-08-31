package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
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
    selectedDish: Dish?,
    quantity: Int = 1,
    canProceed: Boolean,
    onNameChanged: (String) -> Unit,
    onDishSelected: (Dish) -> Unit,
    onQuantityChanged: (Int) -> Unit = {},
    onIncrementQuantity: () -> Unit = {},
    onDecrementQuantity: () -> Unit = {},
    onProceedClicked: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val customColors = AppTheme.customColors

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
        // Soft Ambient Accent Aura in Background
        Box(
            modifier = Modifier
                .size(340.dp)
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

            // Portrait Scrollable Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner: Eyebrow + Serif Italic Greeting + Diya Lamps
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
                        val soundManager = LocalFestiveSoundManager.current
                        val isMuted by (soundManager?.isMuted ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsStateWithLifecycle(false)

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

                // Step 1: User Registration Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = customColors.cardBg
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

                // Step 2: Dish Selection Section (Khandvi & Modak @ 30 Rs)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Festive Delicacy",
                            color = customColors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (selectedDish != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = customColors.primaryAccent,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "${selectedDish.title} Selected",
                                    color = customColors.textOnAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Delicacy Selection Cards (Modak 1pc - ₹40, Khandvi 4pc - ₹30, Combo Plate - ₹55)
                    Dish.entries.forEach { dish ->
                        DishCard(
                            dish = dish,
                            isSelected = selectedDish == dish,
                            onSelect = { onDishSelected(dish) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_${dish.name.lowercase()}")
                        )
                    }
                }

                // Step 3: Number of Quantity Selection (Steppers & Presets)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quantity_selector_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = customColors.cardBg,
                    border = BorderStroke(1.dp, customColors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Number of Quantity",
                                color = customColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Price per unit badge
                            val unitPrice = selectedDish?.pricePerUnit ?: 30
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = customColors.surfaceDark,
                                border = BorderStroke(1.dp, customColors.cardBorder)
                            ) {
                                Text(
                                    text = "₹$unitPrice / Item",
                                    color = customColors.primaryAccent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Quantity Stepper Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Minus Button
                            Surface(
                                onClick = onDecrementQuantity,
                                enabled = quantity > 1,
                                shape = RoundedCornerShape(14.dp),
                                color = if (quantity > 1) customColors.primaryAccent else customColors.surfaceDark,
                                border = BorderStroke(1.dp, customColors.cardBorder),
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("btn_qty_minus")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease Quantity",
                                        tint = if (quantity > 1) customColors.textOnAccent else customColors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Quantity Display Box
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$quantity",
                                    color = customColors.primaryAccent,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (quantity == 1) "Plate / Piece" else "Plates / Pieces",
                                    color = customColors.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Plus Button
                            Surface(
                                onClick = onIncrementQuantity,
                                shape = RoundedCornerShape(14.dp),
                                color = customColors.primaryAccent,
                                border = BorderStroke(1.dp, customColors.primaryAccent),
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("btn_qty_plus")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase Quantity",
                                        tint = customColors.textOnAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Presets: 1, 2, 3, 5, 10
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 5, 10).forEach { preset ->
                                val isSelected = quantity == preset
                                Surface(
                                    onClick = { onQuantityChanged(preset) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) customColors.primaryAccent else customColors.surfaceDark,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) customColors.primaryAccent else customColors.cardBorder
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "$preset",
                                        color = if (isSelected) customColors.textOnAccent else customColors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Total Price Calculation Card
                        val dishPrice = selectedDish?.pricePerUnit ?: 30
                        val totalPrice = dishPrice * quantity
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = customColors.surfaceDark,
                            border = BorderStroke(1.dp, customColors.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = "Price",
                                        tint = customColors.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Total Price ($quantity × ₹$dishPrice):",
                                        color = customColors.textSecondary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "₹$totalPrice",
                                    color = customColors.primaryAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Eligibility Status Pill (Lucky Spin for quantity > 2, Direct Checkout for quantity <= 2)
                val isLuckySpinUnlocked = quantity > 2
                val dishPrice = selectedDish?.pricePerUnit ?: 30
                val totalPrice = dishPrice * quantity

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isLuckySpinUnlocked) customColors.surfaceDark else customColors.surfaceDark,
                    border = BorderStroke(1.dp, if (isLuckySpinUnlocked) customColors.primaryAccent.copy(alpha = 0.5f) else customColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isLuckySpinUnlocked) GreenSuccess else customColors.primaryAccent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isLuckySpinUnlocked) {
                                "🎉 Lucky Spin Unlocked! (Qty $quantity > 2: Spin to win free delicacy)"
                            } else {
                                "💳 Direct Checkout (Order 3+ items to unlock Lucky Spin!)"
                            },
                            color = customColors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Big 3D Tactile CTA Button
                Button(
                    onClick = onProceedClicked,
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp))
                        .testTag("proceed_spin_button"),
                    shape = RoundedCornerShape(18.dp),
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
                            imageVector = if (isLuckySpinUnlocked) Icons.Default.Stars else Icons.Default.ShoppingBag,
                            contentDescription = if (isLuckySpinUnlocked) "Spin" else "Pay",
                            tint = customColors.textOnAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isLuckySpinUnlocked) "PROCEED TO 3D LUCKY SPIN" else "PROCEED TO PAY ₹$totalPrice",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DishCard(
    dish: Dish,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = AppTheme.customColors
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) customColors.primaryAccent else customColors.cardBorder,
        animationSpec = tween(300),
        label = "border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 1.dp,
        animationSpec = tween(300),
        label = "border_width"
    )

    Card(
        onClick = onSelect,
        modifier = modifier
            .shadow(if (isSelected) 12.dp else 3.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) customColors.cardBgSubtle else customColors.cardBg
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish Illustration Graphic Box
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    DishIllustration(
                        dish = dish,
                        modifier = Modifier.fillMaxSize()
                    )
                }

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

                        // Price Pill (₹40, ₹30, ₹55)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = customColors.primaryAccent
                        ) {
                            Text(
                                text = "₹${dish.pricePerUnit}",
                                color = customColors.textOnAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Selection Strip
            Surface(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) customColors.primaryAccent else customColors.surfaceDark,
                border = BorderStroke(1.dp, if (isSelected) customColors.primaryAccent else customColors.cardBorder)
            ) {
                Text(
                    text = if (isSelected) "✓ SELECTED (₹${dish.pricePerUnit})" else "TAP TO SELECT (₹${dish.pricePerUnit})",
                    color = if (isSelected) customColors.textOnAccent else customColors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}
