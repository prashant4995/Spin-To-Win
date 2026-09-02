package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a persistent record of a spin result / food order in the Lucky Spin app.
 * Records items won free via lucky spin as well as items purchased/sold.
 */
@Entity(tableName = "spin_history")
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userName: String,
    val isWin: Boolean,
    val isSold: Boolean = false, // True if item was purchased / sold
    val isFree: Boolean = true,  // True if item was won free via spin
    val quantity: Int = 1,       // Number of items
    val unitPrice: Int = 30,     // Price per unit in ₹ (e.g. Modak 40 Rs)
    val totalAmount: Int = 0,    // Total ₹ paid (0 for free items)
    val dishName: String?,
    val dishNativeTitle: String?,
    val dishSubtitle: String?,
    val dishEmoji: String?,
    val qualityName: String? = null,   // e.g. "Kesar Mawa Premium"
    val qualityBadge: String? = null,  // e.g. "👑 Premium"
    val isPaidViaQr: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


