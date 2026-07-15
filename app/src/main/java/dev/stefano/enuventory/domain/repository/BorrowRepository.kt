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
     * Dipakai di notifikasi admin.
     */
    fun getPendingRequests(): Flow<List<BorrowRecord>>

    /**
     * Mengambil seluruh record peminjaman (semua status).
     * Dipakai di Approval page (tab Pending / Aktif / Selesai).
     */
    fun getAllBorrowRecords(): Flow<List<BorrowRecord>>

    /**
     * Mengambil satu record berdasarkan ID-nya.
     * Dipakai di Detail Riwayat page.
     */
    suspend fun getBorrowRecordById(recordId: String): BorrowRecord?

    /** User mengajukan request peminjaman asset. Tanggal dalam epoch millis. */
    suspend fun requestBorrow(
        assetId: String,
        assetTitle: String,
        userId: String,
        userName: String,
        borrowDate: Long,
        returnEstimate: Long,
        reason: String
    )

    /**
     * Admin menyetujui request + menetapkan jadwal pengambilan.
     * Status pinjam → WaitingPickup, status asset → Reserved (atomik).
     */
    suspend fun approveRequest(recordId: String, assetId: String, pickupSchedule: Long)

    /** Admin menolak request (atau user membatalkan) dengan alasan. */
    suspend fun rejectRequest(recordId: String, rejectionReason: String)

    /** Konfirmasi pengambilan barang (setelah scan QR cocok). Status → Borrowed. */
    suspend fun confirmPickup(recordId: String)

    /**
     * Admin memproses pengembalian.
     * Normal → status Completed + asset Available; rusak → Damaged + asset
     * Maintenance (atomik).
     */
    suspend fun completeReturn(
        recordId: String,
        assetId: String,
        isDamaged: Boolean,
        damageNotes: String?
    )
}
