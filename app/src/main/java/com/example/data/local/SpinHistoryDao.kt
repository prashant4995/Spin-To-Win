package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for querying and persisting spin history, sold items, and free winnings.
 */
@Dao
interface SpinHistoryDao {

    @Query("SELECT * FROM spin_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SpinHistoryEntity>>

    @Query("SELECT * FROM spin_history WHERE isFree = 1 OR isWin = 1 ORDER BY timestamp DESC")
    fun getFreeWinningsHistory(): Flow<List<SpinHistoryEntity>>

    @Query("SELECT * FROM spin_history WHERE isSold = 1 ORDER BY timestamp DESC")
    fun getSoldHistory(): Flow<List<SpinHistoryEntity>>

    @Query("SELECT COUNT(*) FROM spin_history")
    fun getTotalSpinsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM spin_history WHERE isWin = 1 OR isFree = 1")
    fun getTotalWinsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM spin_history WHERE isSold = 1")
    fun getTotalItemsSoldCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM spin_history WHERE isFree = 1")
    fun getTotalItemsFreeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM spin_history WHERE isSold = 1")
    fun getTotalRevenue(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpin(spin: SpinHistoryEntity): Long

    @Query("UPDATE spin_history SET isSold = 1, isPaidViaQr = 1, totalAmount = :amount WHERE id = :id")
    suspend fun markAsPaidViaQr(id: Long, amount: Int)

    @Query("DELETE FROM spin_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM spin_history")
    suspend fun clearAllHistory()
}

