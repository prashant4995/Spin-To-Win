package com.example.data.repository

import com.example.data.local.SpinHistoryDao
import com.example.data.local.SpinHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing a clean API for spin history persistence.
 */
class SpinHistoryRepository(
    private val dao: SpinHistoryDao
) {
    val allHistory: Flow<List<SpinHistoryEntity>> = dao.getAllHistory()
    val winningsHistory: Flow<List<SpinHistoryEntity>> = dao.getWinningsHistory()
    val totalSpinsCount: Flow<Int> = dao.getTotalSpinsCount()
    val totalWinsCount: Flow<Int> = dao.getTotalWinsCount()

    suspend fun insertSpin(spin: SpinHistoryEntity): Long {
        return dao.insertSpin(spin)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }
}
