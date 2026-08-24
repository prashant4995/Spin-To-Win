package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a persistent record of a spin result in the Lucky Spin app.
 */
@Entity(tableName = "spin_history")
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userName: String,
    val isWin: Boolean,
    val dishName: String?,
    val dishNativeTitle: String?,
    val dishSubtitle: String?,
    val dishEmoji: String?,
    val claimCode: String,
    val timestamp: Long = System.currentTimeMillis()
)
