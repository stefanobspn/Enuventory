package dev.stefano.enuventory.fake

import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.repository.BorrowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake untuk [BorrowRepository], dipakai di unit test ViewModel peminjaman
 * & pengembalian. Status di-derive murni dari data, sama seperti Firestore asli.
 * Perubahan status asset (yang di produksi jalan lewat WriteBatch) dicatat di
 * [assetStatusUpdates] supaya test bisa assert transisinya.
 */
class FakeBorrowRepository : BorrowRepository {

    private val recordsFlow = MutableStateFlow<List<BorrowRecord>>(emptyList())
    private var nextId = 1

    var requestBorrowError: Throwable? = null
    var approveRequestError: Throwable? = null
    var rejectRequestError: Throwable? = null
    var confirmPickupError: Throwable? = null
    var completeReturnError: Throwable? = null

    /** Waktu "sekarang" yang deterministik untuk field requestedAt/pickedUpAt/returnDate. */
    var nowMillis: Long = 1_792_195_200_000L

    /** Pasangan assetId → status asset yang ditulis oleh approve/completeReturn. */
    val assetStatusUpdates = mutableListOf<Pair<String, AssetStatus>>()

    fun setRecords(records: List<BorrowRecord>) {
        recordsFlow.value = records
    }

    fun currentRecords(): List<BorrowRecord> = recordsFlow.value

    override fun getBorrowRecordsByUser(userId: String): Flow<List<BorrowRecord>> =
        recordsFlow.map { list -> list.filter { it.borrowerId == userId } }

    override fun getPendingRequests(): Flow<List<BorrowRecord>> =
        recordsFlow.map { list -> list.filter { it.status == BorrowStatus.Pending } }

    override fun getAllBorrowRecords(): Flow<List<BorrowRecord>> = recordsFlow

    override suspend fun getBorrowRecordById(recordId: String): BorrowRecord? =
        recordsFlow.value.find { it.id == recordId }

    override suspend fun requestBorrow(
        assetId: String,
        assetTitle: String,
        userId: String,
        userName: String,
        borrowDate: Long,
        returnEstimate: Long,
        reason: String
    ) {
        requestBorrowError?.let { throw it }
        val record = BorrowRecord(
            id = "record-${nextId++}",
            assetId = assetId,
            assetTitle = assetTitle,
            borrowerId = userId,
            borrowerName = userName,
            status = BorrowStatus.Pending,
            requestedAt = nowMillis,
            borrowDate = borrowDate,
            returnEstimate = returnEstimate,
            reason = reason
        )
        recordsFlow.value = recordsFlow.value + record
    }

    override suspend fun approveRequest(recordId: String, assetId: String, pickupSchedule: Long) {
        approveRequestError?.let { throw it }
        update(recordId) {
            it.copy(
                status = BorrowStatus.WaitingPickup,
                pickupSchedule = pickupSchedule
            )
        }
        assetStatusUpdates += assetId to AssetStatus.Reserved
    }

    override suspend fun rejectRequest(recordId: String, rejectionReason: String) {
        rejectRequestError?.let { throw it }
        update(recordId) {
            it.copy(
                status = BorrowStatus.Rejected,
                rejectionReason = rejectionReason
            )
        }
    }

    override suspend fun confirmPickup(recordId: String) {
        confirmPickupError?.let { throw it }
        update(recordId) { it.copy(status = BorrowStatus.Borrowed, pickedUpAt = nowMillis) }
    }

    override suspend fun completeReturn(
        recordId: String,
        assetId: String,
        isDamaged: Boolean,
        damageNotes: String?
    ) {
        completeReturnError?.let { throw it }
        update(recordId) {
            it.copy(
                status = if (isDamaged) BorrowStatus.Damaged else BorrowStatus.Completed,
                returnDate = nowMillis,
                damageNotes = damageNotes
            )
        }
        assetStatusUpdates += assetId to
                if (isDamaged) AssetStatus.Maintenance else AssetStatus.Available
    }

    private fun update(recordId: String, transform: (BorrowRecord) -> BorrowRecord) {
        recordsFlow.value = recordsFlow.value.map {
            if (it.id == recordId) transform(it) else it
        }
    }
}
