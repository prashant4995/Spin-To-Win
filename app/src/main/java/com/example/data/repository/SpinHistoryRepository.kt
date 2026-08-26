package com.example.data.repository

import com.example.data.local.SpinHistoryDao
import com.example.data.local.SpinHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing a clean API for spin history, item sales, and free winnings persistence.
 */
class SpinHistoryRepository(
    private val dao: SpinHistoryDao
) {
    val allHistory: Flow<List<SpinHistoryEntity>> = dao.getAllHistory()
    val freeWinningsHistory: Flow<List<SpinHistoryEntity>> = dao.getFreeWinningsHistory()
    val soldHistory: Flow<List<SpinHistoryEntity>> = dao.getSoldHistory()
    val totalSpinsCount: Flow<Int> = dao.getTotalSpinsCount()
    val totalWinsCount: Flow<Int> = dao.getTotalWinsCount()
    val totalItemsSoldCount: Flow<Int> = dao.getTotalItemsSoldCount()
    val totalItemsFreeCount: Flow<Int> = dao.getTotalItemsFreeCount()
    val totalRevenue: Flow<Int> = dao.getTotalRevenue()

    suspend fun insertSpin(spin: SpinHistoryEntity): Long {
        return dao.insertSpin(spin)
    }

    suspend fun markAsPaidViaQr(id: Long, amount: Int) {
        dao.markAsPaidViaQr(id, amount)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }
}

