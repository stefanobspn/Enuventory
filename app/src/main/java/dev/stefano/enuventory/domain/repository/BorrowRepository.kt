package dev.stefano.enuventory.domain.repository

import dev.stefano.enuventory.domain.model.BorrowRecord
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk semua operasi data yang berhubungan dengan peminjaman.
 *
 * Interface ini ada di domain layer — implementasinya ada di data layer.
 */
interface BorrowRepository {

    /**
     * Mengambil semua record peminjaman milik user tertentu.
     * Dipakai di History page (user melihat riwayat pinjamannya sendiri).
     */
    fun getBorrowRecordsByUser(userId: String): Flow<List<BorrowRecord>>

    /**
     * Mengambil semua record peminjaman yang statusnya Pending.
     * Dipakai di Approval page (admin melihat request yang perlu disetujui).
     */
    fun getPendingRequests(): Flow<List<BorrowRecord>>

    /**
     * Mengambil satu record berdasarkan ID-nya.
     * Dipakai di Detail Riwayat page.
     */
    suspend fun getBorrowRecordById(recordId: String): BorrowRecord?

    /** User mengajukan request peminjaman asset. */
    suspend fun requestBorrow(
        assetId: String,
        assetTitle: String,
        assetStock: Int,
        userId: String,
        userName: String,
        returnEstimate: String
    )

    /** Admin menyetujui request peminjaman. */
    suspend fun approveRequest(recordId: String)

    /** Admin menolak request peminjaman. */
    suspend fun rejectRequest(recordId: String)

    /** User/admin menandai asset telah dikembalikan. */
    suspend fun returnAsset(recordId: String, proofImageUrl: String? = null)
}
