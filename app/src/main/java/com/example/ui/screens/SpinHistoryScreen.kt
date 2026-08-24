package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfied
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
import com.example.ui.theme.MaroonRoyal
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpinHistoryScreen(
    historyList: List<SpinHistoryEntity>,
    filterOnlyWins: Boolean,
    totalSpins: Int,
    totalWins: Int,
    onFilterChanged: (Boolean) -> Unit,
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

    val displayList = remember(historyList, filterOnlyWins) {
        if (filterOnlyWins) historyList.filter { it.isWin } else historyList
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        text = "Winnings & History",
                        color = ArtisticAmberGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "विजय आणि इतिहास",
                        color = ArtisticCreamSub,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
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

            // --- SUMMARY STATS BANNER ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisticMaroonCard),
                border = BorderStroke(1.5.dp, FestiveCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ArtisticMaroonCard,
                                    ArtisticMaroonSurface
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Spins
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalSpins",
                                color = ArtisticCream,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Total Spins",
                            color = ArtisticCreamSub,
                            fontSize = 11.sp
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(ArtisticAmberSubtle)
                    )

                    // Total Wins
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = ArtisticAmberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalWins",
                                color = ArtisticAmberGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Free Dishes Won",
                            color = ArtisticCreamSub,
                            fontSize = 11.sp
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(ArtisticAmberSubtle)
                    )

                    // Win Rate %
                    val winRate = if (totalSpins > 0) (totalWins * 100 / totalSpins) else 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$winRate%",
                                color = Color(0xFF69F0AE),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Win Rate",
                            color = ArtisticCreamSub,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // --- FILTER TABS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !filterOnlyWins,
                    onClick = {
                        soundManager?.playClickSound()
                        onFilterChanged(false)
                    },
                    label = {
                        Text(
                            text = "All Spins (${historyList.size})",
                            fontSize = 12.sp,
                            fontWeight = if (!filterOnlyWins) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
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
                    border = BorderStroke(1.dp, if (!filterOnlyWins) ArtisticAmberGold else ArtisticAmberSubtle),
                    modifier = Modifier.testTag("filter_all_spins")
                )

                FilterChip(
                    selected = filterOnlyWins,
                    onClick = {
                        soundManager?.playClickSound()
                        onFilterChanged(true)
                    },
                    label = {
                        Text(
                            text = "🏆 Winnings ($totalWins)",
                            fontSize = 12.sp,
                            fontWeight = if (filterOnlyWins) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
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
                    border = BorderStroke(1.dp, if (filterOnlyWins) ArtisticAmberGold else ArtisticAmberSubtle),
                    modifier = Modifier.testTag("filter_winnings_only")
                )
            }

            // --- HISTORY LIST OR EMPTY STATE ---
            if (displayList.isEmpty()) {
                EmptyHistoryView(
                    filterOnlyWins = filterOnlyWins,
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = displayList,
                        key = { it.id }
                    ) { item ->
                        SpinHistoryCard(
                            item = item,
                            onCopyCode = { code ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Claim Voucher", code)
                                clipboard.setPrimaryClip(clip)
                                soundManager?.playClaimChime()
                                Toast.makeText(context, "Voucher code '$code' copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
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
                        text = "Clear Spin History?",
                        color = ArtisticAmberGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Text(
                        text = "This will permanently remove all previous spin records and saved winning voucher codes from the local database.",
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
                            Toast.makeText(context, "Spin history cleared", Toast.LENGTH_SHORT).show()
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
 * Rich Material 3 card presenting an individual spin history record.
 */
@Composable
private fun SpinHistoryCard(
    item: SpinHistoryEntity,
    onCopyCode: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWin = item.isWin
    val formattedDate = remember(item.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisticMaroonCard),
        border = BorderStroke(
            width = if (isWin) 1.8.dp else 1.dp,
            color = if (isWin) ArtisticAmberGold else FestiveCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isWin) {
                            listOf(
                                ArtisticMaroonCard,
                                ArtisticMaroonSurface
                            )
                        } else {
                            listOf(
                                ArtisticMaroonCard,
                                ArtisticMaroonBg
                            )
                        }
                    )
                )
                .padding(14.dp)
        ) {
            // Header Row: Status Badge + Date + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isWin) MaroonRoyal else SaffronDark.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, if (isWin) ArtisticAmberGold else SaffronPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isWin) Icons.Default.Celebration else Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (isWin) ArtisticAmberGold else ArtisticCream,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isWin) "JACKPOT WIN" else "TRY AGAIN",
                            color = if (isWin) ArtisticAmberGold else ArtisticCream,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        color = ArtisticCreamSub,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = ArtisticCreamSub.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Guest Name & Dish Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish Emoji Avatar or Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isWin) ArtisticAmberContainer else ArtisticMaroonSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.dishEmoji ?: if (isWin) "🍲" else "🎡",
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isWin) "Won: ${item.dishName ?: "Festive Delicacy"}" else "Played by ${item.userName}",
                        color = if (isWin) ArtisticAmberGold else ArtisticCream,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isWin && !item.dishNativeTitle.isNullOrBlank()) {
                        Text(
                            text = "${item.dishNativeTitle} • Winner: ${item.userName}",
                            color = ArtisticCreamSub,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (!isWin) {
                        Text(
                            text = "Better luck on the next festive spin!",
                            color = ArtisticCreamSub,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // If Win: Voucher Claim Code Section
            if (isWin && item.claimCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArtisticMaroonDark,
                    border = BorderStroke(1.dp, ArtisticAmberGold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CLAIM VOUCHER CODE",
                                color = ArtisticAmberGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = item.claimCode,
                                color = ArtisticCream,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { onCopyCode(item.claimCode) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ArtisticAmberGold)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Voucher Code",
                                tint = ArtisticMaroonDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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
    filterOnlyWins: Boolean,
    onStartSpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DiyaLamp(modifier = Modifier.size(64.dp))

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (filterOnlyWins) "No Winnings Yet" else "No Spin History",
            color = ArtisticAmberGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (filterOnlyWins) {
                "Spin the 3D lucky wheel to unlock free Maharashtrian delicacies & collect your winning vouchers!"
            } else {
                "Your spin history and winning vouchers will be recorded here locally in real-time."
            },
            color = ArtisticCreamSub,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartSpin,
            colors = ButtonDefaults.buttonColors(
                containerColor = ArtisticAmberGold,
                contentColor = ArtisticMaroonDark
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            modifier = Modifier.testTag("empty_history_spin_button")
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Spin The Wheel Now",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
