package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.local.SpinHistoryEntity
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
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MaroonRoyal
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.viewmodel.HistoryFilterType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpinHistoryScreen(
    historyList: List<SpinHistoryEntity>,
    filterType: HistoryFilterType = HistoryFilterType.ALL,
    totalSpins: Int = 0,
    totalWins: Int = 0,
    totalItemsSold: Int = 0,
    totalItemsFree: Int = 0,
    totalRevenue: Int = 0,
    modakSoldCount: Int = 0,
    modakFreeCount: Int = 0,
    khandviSoldCount: Int = 0,
    khandviFreeCount: Int = 0,
    onFilterTypeChanged: (HistoryFilterType) -> Unit = {},
    onDeleteHistoryItem: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    onStartSpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = LocalFestiveSoundManager.current
    val isMuted by (soundManager?.isMuted ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsStateWithLifecycle(false)

    var showClearDialog by remember { mutableStateOf(false) }

    val displayList = remember(historyList, filterType) {
        when (filterType) {
            HistoryFilterType.ALL -> historyList
            HistoryFilterType.SOLD -> historyList.filter { it.isSold }
            HistoryFilterType.FREE -> historyList.filter { it.isFree || it.isWin }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ArtisticMaroonDark,
                        ArtisticMaroonBg,
                        ArtisticMaroonSurface
                    )
                )
            )
    ) {
        // Decorative top marigold garland
        MarigoldGarland(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp)
        ) {
            // --- TOP NAVIGATION BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        soundManager?.playClickSound()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ArtisticMaroonCard)
                        .testTag("history_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArtisticAmberGold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sales & Free Treats History",
                        color = ArtisticAmberGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "विक्री आणि मोफत प्रसाद इतिहास",
                        color = ArtisticCreamSub,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                soundManager?.playClickSound()
                                showClearDialog = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ArtisticMaroonCard)
                                .testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = { soundManager?.toggleMute() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ArtisticMaroonCard)
                            .testTag("sound_toggle_history_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) ArtisticCreamSub else ArtisticAmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- SUMMARY STATS BANNER (Requirement 6: Sold & Free Items History) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .shadow(10.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisticMaroonCard),
                border = BorderStroke(1.5.dp, FestiveCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Metric Row: Sold, Free, Revenue
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Sold
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$totalItemsSold",
                                    color = SaffronPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "Items Sold",
                                color = ArtisticCreamSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(ArtisticAmberSubtle)
                        )

                        // Total Free Prasad
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = ArtisticAmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$totalItemsFree",
                                    color = ArtisticAmberGold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "Free Prasad Won",
                                color = ArtisticCreamSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(ArtisticAmberSubtle)
                        )

                        // Total Revenue
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹",
                                    color = GreenSuccess,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "$totalRevenue",
                                    color = GreenSuccess,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "Total Revenue",
                                color = ArtisticCreamSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Delicacy specific breakdown bar: Modak & Khandvi
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ArtisticMaroonDark,
                        border = BorderStroke(0.8.dp, ArtisticAmberSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🥟 Modak: $modakSoldCount Sold • $modakFreeCount Free",
                                color = ArtisticCream,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🥮 Khandvi: $khandviSoldCount Sold • $khandviFreeCount Free",
                                color = ArtisticAmberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // --- FILTER TABS: All / Sold / Free ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Tab
                FilterChip(
                    selected = filterType == HistoryFilterType.ALL,
                    onClick = {
                        soundManager?.playClickSound()
                        onFilterTypeChanged(HistoryFilterType.ALL)
                    },
                    label = {
                        Text(
                            text = "All (${historyList.size})",
                            fontSize = 11.5.sp,
                            fontWeight = if (filterType == HistoryFilterType.ALL) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronDark,
                        selectedLabelColor = ArtisticCream,
                        selectedLeadingIconColor = ArtisticAmberGold,
                        containerColor = ArtisticMaroonCard,
                        labelColor = ArtisticCreamSub,
                        iconColor = ArtisticCreamSub
                    ),
                    border = BorderStroke(1.dp, if (filterType == HistoryFilterType.ALL) ArtisticAmberGold else ArtisticAmberSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_all_spins")
                )

                // Sold Items Tab
                val soldTotal = historyList.count { it.isSold }
                FilterChip(
                    selected = filterType == HistoryFilterType.SOLD,
                    onClick = {
                        soundManager?.playClickSound()
                        onFilterTypeChanged(HistoryFilterType.SOLD)
                    },
                    label = {
                        Text(
                            text = "💰 Sold ($soldTotal)",
                            fontSize = 11.5.sp,
                            fontWeight = if (filterType == HistoryFilterType.SOLD) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonRoyal,
                        selectedLabelColor = ArtisticAmberGold,
                        selectedLeadingIconColor = ArtisticAmberGold,
                        containerColor = ArtisticMaroonCard,
                        labelColor = ArtisticCreamSub,
                        iconColor = ArtisticCreamSub
                    ),
                    border = BorderStroke(1.dp, if (filterType == HistoryFilterType.SOLD) ArtisticAmberGold else ArtisticAmberSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_sold_items")
                )

                // Free Prasad Tab
                val freeTotal = historyList.count { it.isFree || it.isWin }
                FilterChip(
                    selected = filterType == HistoryFilterType.FREE,
                    onClick = {
                        soundManager?.playClickSound()
                        onFilterTypeChanged(HistoryFilterType.FREE)
                    },
                    label = {
                        Text(
                            text = "🎁 Free ($freeTotal)",
                            fontSize = 11.5.sp,
                            fontWeight = if (filterType == HistoryFilterType.FREE) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonRoyal,
                        selectedLabelColor = ArtisticAmberGold,
                        selectedLeadingIconColor = ArtisticAmberGold,
                        containerColor = ArtisticMaroonCard,
                        labelColor = ArtisticCreamSub,
                        iconColor = ArtisticCreamSub
                    ),
                    border = BorderStroke(1.dp, if (filterType == HistoryFilterType.FREE) ArtisticAmberGold else ArtisticAmberSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_free_items")
                )
            }

            // --- HISTORY LIST OR EMPTY STATE ---
            if (displayList.isEmpty()) {
                EmptyHistoryView(
                    filterType = filterType,
                    onStartSpin = {
                        soundManager?.playClickSound()
                        onStartSpin()
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("spin_history_list"),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = displayList,
                        key = { it.id }
                    ) { item ->
                        SpinHistoryCard(
                            item = item,
                            onDelete = {
                                soundManager?.playClickSound()
                                onDeleteHistoryItem(item.id)
                            }
                        )
                    }
                }
            }
        }

        // --- CLEAR ALL CONFIRMATION DIALOG ---
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Clear All Records?",
                        color = ArtisticAmberGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Text(
                        text = "This will permanently clear all sold orders, free winnings, and revenue records from local memory.",
                        color = ArtisticCream,
                        fontSize = 14.sp
                    )
                },
                containerColor = ArtisticMaroonDark,
                shape = RoundedCornerShape(20.dp),
                confirmButton = {
                    Button(
                        onClick = {
                            showClearDialog = false
                            onClearAllHistory()
                            Toast.makeText(context, "All history records cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_clear_history_button")
                    ) {
                        Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearDialog = false }
                    ) {
                        Text("Cancel", color = ArtisticCreamSub)
                    }
                }
            )
        }
    }
}

/**
 * Rich Material 3 card presenting an individual sales/free dish history record.
 */
@Composable
private fun SpinHistoryCard(
    item: SpinHistoryEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFree = item.isFree || item.isWin
    val isSold = item.isSold
    val formattedDate = remember(item.timestamp) {
        val sdf = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisticMaroonCard),
        border = BorderStroke(
            width = if (isFree) 1.5.dp else if (isSold) 1.2.dp else 1.dp,
            color = if (isFree) ArtisticAmberGold else if (isSold) GreenSuccess else FestiveCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isFree) {
                            listOf(ArtisticMaroonCard, ArtisticMaroonSurface)
                        } else {
                            listOf(ArtisticMaroonCard, ArtisticMaroonBg)
                        }
                    )
                )
                .padding(12.dp)
        ) {
            // Header Row: Status Badge + Date + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isFree -> MaroonRoyal
                        isSold -> GreenSuccess.copy(alpha = 0.2f)
                        else -> SaffronDark.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isFree -> ArtisticAmberGold
                            isSold -> GreenSuccess
                            else -> SaffronPrimary
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isFree -> Icons.Default.Celebration
                                isSold -> Icons.Default.CheckCircle
                                else -> Icons.Default.Refresh
                            },
                            contentDescription = null,
                            tint = when {
                                isFree -> ArtisticAmberGold
                                isSold -> GreenSuccess
                                else -> ArtisticCream
                            },
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isFree -> "🎁 FREE PRASAD"
                                isSold -> "💰 SOLD (PAID VIA QR)"
                                else -> "TRY AGAIN SPIN"
                            },
                            color = when {
                                isFree -> ArtisticAmberGold
                                isSold -> GreenSuccess
                                else -> ArtisticCream
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        color = ArtisticCreamSub,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = ArtisticCreamSub.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body: Guest Name, Quantity, Delicacy & Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish Emoji Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isFree) ArtisticAmberContainer else ArtisticMaroonDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.dishEmoji ?: if (isFree) "🥟" else "🥮",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.quantity}x ${item.dishName ?: "Delicacy"} • ${item.userName}",
                        color = if (isFree) ArtisticAmberGold else ArtisticCream,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isFree) {
                            "Free Lucky Winning Prasad • ₹0 (Worth ₹${item.quantity * item.unitPrice})"
                        } else if (isSold) {
                            "Paid via UPI QR • ₹${item.totalAmount} (${item.quantity} × ₹${item.unitPrice})"
                        } else {
                            "${item.quantity} items requested @ ₹${item.unitPrice} each"
                        },
                        color = ArtisticCreamSub,
                        fontSize = 11.5.sp
                    )
                }

                // Amount Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ArtisticMaroonDark,
                    border = BorderStroke(0.8.dp, if (isSold) GreenSuccess else ArtisticAmberSubtle)
                ) {
                    Text(
                        text = if (isFree) "FREE" else "₹${if (isSold) item.totalAmount else item.quantity * item.unitPrice}",
                        color = if (isFree) ArtisticAmberGold else if (isSold) GreenSuccess else ArtisticCream,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state screen when no history entries are present.
 */
@Composable
private fun EmptyHistoryView(
    filterType: HistoryFilterType,
    onStartSpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DiyaLamp(modifier = Modifier.size(56.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (filterType) {
                HistoryFilterType.ALL -> "No Records Yet"
                HistoryFilterType.SOLD -> "No Sold Items Yet"
                HistoryFilterType.FREE -> "No Free Prasad Won Yet"
            },
            color = ArtisticAmberGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Customer orders, QR payments, and winning free delicacies will appear here in real-time.",
            color = ArtisticCreamSub,
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onStartSpin,
            colors = ButtonDefaults.buttonColors(
                containerColor = ArtisticAmberGold,
                contentColor = ArtisticMaroonDark
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            modifier = Modifier.testTag("empty_history_spin_button")
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Take Next Customer / Spin",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
