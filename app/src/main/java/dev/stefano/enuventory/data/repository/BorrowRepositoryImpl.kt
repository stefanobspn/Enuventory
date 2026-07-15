package dev.stefano.enuventory.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.repository.BorrowRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BorrowRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BorrowRepository {

    private val borrowsCollection = firestore.collection("borrows")
    private val assetsCollection = firestore.collection("assets")

    override fun getBorrowRecordsByUser(userId: String): Flow<List<BorrowRecord>> = callbackFlow {
        val listener = borrowsCollection
            .whereEqualTo("borrowerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val records = snapshot.documents.mapNotNull { it.toBorrowRecord() }
                trySend(records.sortedByDescending { it.requestedAt })
            }
        awaitClose { listener.remove() }
    }

    override fun getPendingRequests(): Flow<List<BorrowRecord>> = callbackFlow {
        val listener = borrowsCollection
            .whereEqualTo("status", BorrowStatus.Pending.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val records = snapshot.documents.mapNotNull { it.toBorrowRecord() }
                trySend(records.sortedByDescending { it.requestedAt })
            }
        awaitClose { listener.remove() }
    }

    override fun getAllBorrowRecords(): Flow<List<BorrowRecord>> = callbackFlow {
        // Tanpa orderBy — sorting in-memory agar tidak butuh composite index
        val listener = borrowsCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val records = snapshot.documents.mapNotNull { it.toBorrowRecord() }
            trySend(records.sortedByDescending { it.requestedAt })
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getBorrowRecordById(recordId: String): BorrowRecord? {
        return borrowsCollection.document(recordId).get().await().toBorrowRecord()
    }

    override suspend fun requestBorrow(
        assetId: String,
        assetTitle: String,
        userId: String,
        userName: String,
        borrowDate: Long,
        returnEstimate: Long,
        reason: String
    ) {
        val newDoc = borrowsCollection.document()
        val data = mapOf(
            "id" to newDoc.id,
            "assetId" to assetId,
            "assetTitle" to assetTitle,
            "borrowerId" to userId,
            "borrowerName" to userName,
            "status" to BorrowStatus.Pending.name,
            "requestedAt" to Timestamp.now(),
            "borrowDate" to Timestamp(Date(borrowDate)),
            "returnEstimate" to Timestamp(Date(returnEstimate)),
            "reason" to reason
        )
        newDoc.set(data).await()
    }

    override suspend fun approveRequest(recordId: String, assetId: String, pickupSchedule: Long) {
        // Batch agar status pinjam & status asset berubah atomik
        firestore.batch().apply {
            update(
                borrowsCollection.document(recordId),
                mapOf(
                    "status" to BorrowStatus.WaitingPickup.name,
                    "pickupSchedule" to Timestamp(Date(pickupSchedule))
                )
            )
            update(assetsCollection.document(assetId), "status", AssetStatus.Reserved.name)
        }.commit().await()
    }

    override suspend fun rejectRequest(recordId: String, rejectionReason: String) {
        borrowsCollection.document(recordId)
            .update(
                mapOf(
                    "status" to BorrowStatus.Rejected.name,
                    "rejectionReason" to rejectionReason
                )
            )
            .await()
    }

    override suspend fun confirmPickup(recordId: String) {
        borrowsCollection.document(recordId)
            .update(
                mapOf(
                    "status" to BorrowStatus.Borrowed.name,
                    "pickedUpAt" to Timestamp.now()
                )
            )
            .await()
    }

    override suspend fun completeReturn(
        recordId: String,
        assetId: String,
        isDamaged: Boolean,
        damageNotes: String?
    ) {
        val borrowUpdates = mutableMapOf<String, Any>(
            "status" to (if (isDamaged) BorrowStatus.Damaged else BorrowStatus.Completed).name,
            "returnDate" to Timestamp.now()
        )
        if (isDamaged && !damageNotes.isNullOrBlank()) {
            borrowUpdates["damageNotes"] = damageNotes
        }
        val assetStatus = if (isDamaged) AssetStatus.Maintenance else AssetStatus.Available

        firestore.batch().apply {
            update(borrowsCollection.document(recordId), borrowUpdates)
            update(assetsCollection.document(assetId), "status", assetStatus.name)
        }.commit().await()
    }

    // ── Private mapper helpers ──────────────────────────────────────────────

    private fun DocumentSnapshot.toBorrowRecord(): BorrowRecord? {
        if (!exists()) return null
        return try {
            val requestedAt = dateMillis("requestedAt")
            // Dokumen lama tidak punya requestedAt — pakai borrowDate lamanya
            val borrowDate = dateMillis("borrowDate")
            BorrowRecord(
                id = id,
                assetId = getString("assetId") ?: return null,
                assetTitle = getString("assetTitle") ?: "",
                borrowerId = getString("borrowerId") ?: return null,
                borrowerName = getString("borrowerName") ?: "",
                status = BorrowStatus.fromRaw(getString("status")),
                requestedAt = requestedAt ?: borrowDate ?: 0L,
                borrowDate = borrowDate ?: 0L,
                returnEstimate = dateMillis("returnEstimate") ?: 0L,
                reason = getString("reason") ?: "",
                pickupSchedule = dateMillis("pickupSchedule"),
                pickedUpAt = dateMillis("pickedUpAt"),
                returnDate = dateMillis("returnDate"),
                rejectionReason = getString("rejectionReason"),
                damageNotes = getString("damageNotes")
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Membaca field tanggal sebagai epoch millis.
     * Dokumen baru menyimpan Timestamp; dokumen lama menyimpan String
     * ("dd MMM yyyy, HH:mm" atau "dd MMM yyyy") — keduanya tetap didukung.
     */
    private fun DocumentSnapshot.dateMillis(field: String): Long? =
        when (val raw = get(field)) {
            is Timestamp -> raw.toDate().time
            is String -> LEGACY_DATE_PATTERNS.firstNotNullOfOrNull { pattern ->
                try {
                    SimpleDateFormat(pattern, Locale.getDefault()).parse(raw)?.time
                } catch (e: Exception) {
                    null
                }
            }

            else -> null
        }

    private companion object {
        val LEGACY_DATE_PATTERNS = listOf("dd MMM yyyy, HH:mm", "dd MMM yyyy")
    }
}
