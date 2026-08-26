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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
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
import com.example.ui.theme.GoldLight
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
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
        // Soft Ambient Gold Aura in Background
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ArtisticAmberGlow.copy(alpha = 0.12f),
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
                    color = ArtisticMaroonCard,
                    border = BorderStroke(1.2.dp, FestiveCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val soundManager = LocalFestiveSoundManager.current
                        val isMuted by (soundManager?.isMuted ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsStateWithLifecycle(false)

                        IconButton(
                            onClick = { soundManager?.toggleMute() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ArtisticMaroonDark)
                                .testTag("sound_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute festive sounds" else "Mute festive sounds",
                                tint = if (isMuted) ArtisticCreamSub else ArtisticAmberGold,
                                modifier = Modifier.size(20.dp)
                            )
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
                                    text = "|| ॐ श्री गणेशाय नमः ||",
                                    color = ArtisticAmberGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                GaneshaIdolIcon(modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = if (userName.isNotBlank()) "Namaste, $userName!" else "Namaste & Welcome!",
                                color = ArtisticAmberGold,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    soundManager?.playClickSound()
                                    onOpenHistory()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ArtisticMaroonDark)
                                    .testTag("history_nav_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "View History & Winnings",
                                    tint = ArtisticAmberGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            DiyaLamp(modifier = Modifier.size(32.dp))
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
                        containerColor = ArtisticMaroonCard.copy(alpha = 0.96f)
                    ),
                    border = BorderStroke(1.2.dp, FestiveCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "STEP 1 OF 2",
                                    color = ArtisticAmberGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "Enter Your Name",
                                    color = ArtisticCream,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                            }

                            // Monogram Avatar Badge
                            val initialLetter = if (userName.isNotBlank()) userName.first().uppercase() else "✦"
                            Surface(
                                shape = CircleShape,
                                color = ArtisticAmberContainer,
                                border = BorderStroke(2.dp, ArtisticAmberGold),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initialLetter,
                                        color = ArtisticCream,
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
                                Text(text = "e.g., Ananya Sharma", color = Color(0xFF9E8A8F))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Name",
                                    tint = ArtisticAmberGold
                                )
                            },
                            trailingIcon = {
                                if (userName.isNotEmpty()) {
                                    IconButton(onClick = { onNameChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Name",
                                            tint = ArtisticCreamSub
                                        )
                                    }
                                }
                            },
                            isError = nameError != null,
                            supportingText = {
                                if (nameError != null) {
                                    Text(text = nameError, color = Color(0xFFFF8A80), fontSize = 11.sp)
                                } else {
                                    Text(
                                        text = "Your name will be printed on the official food claim ticket",
                                        color = Color(0xFF9E8A8F),
                                        fontSize = 10.5.sp
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ArtisticCream,
                                unfocusedTextColor = ArtisticCream,
                                focusedBorderColor = ArtisticAmberGold,
                                unfocusedBorderColor = ArtisticAmberSubtle,
                                focusedContainerColor = ArtisticMaroonDark,
                                unfocusedContainerColor = ArtisticMaroonDark,
                                cursorColor = ArtisticAmberGold
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
                        Column {
                            Text(
                                text = "STEP 2 OF 3",
                                color = ArtisticAmberGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Select Festive Delicacy",
                                color = ArtisticCream,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (selectedDish != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ArtisticAmberGold,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "${selectedDish.title} Selected",
                                    color = ArtisticMaroonBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Khandvi Card
                    DishCard(
                        dish = Dish.KHANDVI,
                        isSelected = selectedDish == Dish.KHANDVI,
                        onSelect = { onDishSelected(Dish.KHANDVI) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_khandvi")
                    )

                    // Modak Card (Fixed Price ₹30 Rs)
                    DishCard(
                        dish = Dish.MODAK,
                        isSelected = selectedDish == Dish.MODAK,
                        onSelect = { onDishSelected(Dish.MODAK) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_modak")
                    )
                }

                // Step 3: Number of Quantity Selection (Steppers & Presets)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quantity_selector_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = ArtisticMaroonCard,
                    border = BorderStroke(1.dp, FestiveCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "STEP 3 OF 3",
                                    color = ArtisticAmberGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "Number of Quantity",
                                    color = ArtisticCream,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Price per unit badge
                            val unitPrice = selectedDish?.pricePerUnit ?: 30
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ArtisticAmberContainer,
                                border = BorderStroke(1.dp, ArtisticAmberGold)
                            ) {
                                Text(
                                    text = "₹$unitPrice / Item",
                                    color = ArtisticCream,
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
                                shape = RoundedCornerShape(12.dp),
                                color = if (quantity > 1) ArtisticAmberGold else ArtisticMaroonDark,
                                border = BorderStroke(1.dp, ArtisticAmberSubtle),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = quantity > 1, onClick = onDecrementQuantity)
                                    .testTag("btn_qty_minus")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease Quantity",
                                        tint = if (quantity > 1) ArtisticMaroonBg else Color(0xFF6B4A4A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Quantity Display Box
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$quantity",
                                    color = GoldLight,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (quantity == 1) "Plate / Piece" else "Plates / Pieces",
                                    color = ArtisticCreamSub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Plus Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ArtisticAmberGold,
                                border = BorderStroke(1.dp, ArtisticAmberGold),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onIncrementQuantity)
                                    .testTag("btn_qty_plus")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase Quantity",
                                        tint = ArtisticMaroonBg,
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
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ArtisticAmberGold else ArtisticMaroonDark,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) ArtisticAmberGold else ArtisticAmberSubtle
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onQuantityChanged(preset) }
                                ) {
                                    Text(
                                        text = "$preset",
                                        color = if (isSelected) ArtisticMaroonBg else ArtisticCream,
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
                            color = ArtisticMaroonDark,
                            border = BorderStroke(1.dp, ArtisticAmberSubtle),
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
                                        tint = ArtisticAmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Total Price ($quantity × ₹$dishPrice):",
                                        color = ArtisticCreamSub,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "₹$totalPrice",
                                    color = ArtisticAmberGlow,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Eligibility Status Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ArtisticAmberContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, ArtisticAmberSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (canProceed) GreenSuccess else ArtisticAmberGold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (canProceed) "✓ Ready to Spin! 1 Free Spin Unlocked + QR Payment Available" else "Enter name and select a delicacy to unlock spin",
                            color = ArtisticCream,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Big 3D Tactile CTA Button
                Button(
                    onClick = onProceedClicked,
                    enabled = canProceed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(if (canProceed) 10.dp else 0.dp, RoundedCornerShape(18.dp))
                        .testTag("proceed_spin_button"),
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
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Spin",
                            tint = if (canProceed) ArtisticMaroonBg else Color(0xFF7A4A4A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PROCEED TO 3D LUCKY SPIN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
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
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ArtisticAmberGold else FestiveCardBorder,
        animationSpec = tween(300),
        label = "border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 1.dp,
        animationSpec = tween(300),
        label = "border_width"
    )

    Card(
        modifier = modifier
            .shadow(if (isSelected) 12.dp else 3.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ArtisticMaroonSurface else ArtisticMaroonCard
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish Illustration Graphic Box
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    DishIllustration(
                        dish = dish,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Dish Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ArtisticMaroonDark,
                            border = BorderStroke(0.8.dp, ArtisticAmberGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = dish.tag,
                                color = ArtisticAmberGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Fixed Price Pill (₹30 Rs)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ArtisticAmberGold
                        ) {
                            Text(
                                text = "₹${dish.pricePerUnit} Rs",
                                color = ArtisticMaroonBg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dish.title,
                        color = ArtisticCream,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${dish.nativeTitle} • ${dish.subtitle}",
                        color = ArtisticAmberGold,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dish.description,
                        color = ArtisticCreamSub.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Highlight Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dish.highlights.forEach { highlight ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ArtisticMaroonDark,
                        border = BorderStroke(0.5.dp, ArtisticAmberSubtle)
                    ) {
                        Text(
                            text = "• $highlight",
                            color = ArtisticAmberGold,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Selection Strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) ArtisticAmberGold else ArtisticMaroonDark,
                border = BorderStroke(1.dp, if (isSelected) ArtisticAmberGold else ArtisticAmberSubtle)
            ) {
                Text(
                    text = if (isSelected) "✓ SELECTED TARGET DELICACY (₹${dish.pricePerUnit})" else "TAP TO SELECT AS TARGET (₹${dish.pricePerUnit})",
                    color = if (isSelected) ArtisticMaroonBg else ArtisticCreamSub.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 7.dp)
                )
            }
        }
    }
}

