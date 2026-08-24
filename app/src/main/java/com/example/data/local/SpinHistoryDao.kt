package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for querying and persisting spin history and previous winnings.
 */
@Dao
interface SpinHistoryDao {

    @Query("SELECT * FROM spin_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SpinHistoryEntity>>

    @Query("SELECT * FROM spin_history WHERE isWin = 1 ORDER BY timestamp DESC")
    fun getWinningsHistory(): Flow<List<SpinHistoryEntity>>

    @Query("SELECT COUNT(*) FROM spin_history")
    fun getTotalSpinsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM spin_history WHERE isWin = 1")
    fun getTotalWinsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpin(spin: SpinHistoryEntity): Long

    @Query("DELETE FROM spin_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM spin_history")
    suspend fun clearAllHistory()
}
