package dev.stefano.enuventory.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.repository.BorrowRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BorrowRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BorrowRepository {

    private val borrowsCollection = firestore.collection("borrows")

    override fun getBorrowRecordsByUser(userId: String): Flow<List<BorrowRecord>> = callbackFlow {
        val listener = borrowsCollection
            .whereEqualTo("borrowerId", userId)
            .orderBy("borrowDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot.documents.mapNotNull { it.toBorrowRecord() })
            }
        awaitClose { listener.remove() }
    }

    override fun getPendingRequests(): Flow<List<BorrowRecord>> = callbackFlow {
        val listener = borrowsCollection
            .whereEqualTo("status", BorrowStatus.Pending.name)
            .orderBy("borrowDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot.documents.mapNotNull { it.toBorrowRecord() })
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getBorrowRecordById(recordId: String): BorrowRecord? {
        return borrowsCollection.document(recordId).get().await().toBorrowRecord()
    }

    override suspend fun requestBorrow(
        assetId: String,
        assetTitle: String,
        assetStock: Int,
        userId: String,
        userName: String,
        returnEstimate: String
    ) {
        val newDoc = borrowsCollection.document()
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        val formattedBorrowDate = sdf.format(java.util.Date())

        val data = mapOf(
            "id" to newDoc.id,
            "assetId" to assetId,
            "assetTitle" to assetTitle,
            "assetStock" to assetStock,
            "borrowerId" to userId,
            "borrowerName" to userName,
            "status" to BorrowStatus.Pending.name,
            "borrowDate" to formattedBorrowDate,
            "returnEstimate" to returnEstimate
        )
        newDoc.set(data).await()
    }

    override suspend fun approveRequest(recordId: String) {
        borrowsCollection.document(recordId)
            .update("status", BorrowStatus.Borrowed.name)
            .await()
    }

    override suspend fun rejectRequest(recordId: String) {
        borrowsCollection.document(recordId)
            .update("status", BorrowStatus.Rejected.name)
            .await()
    }

    override suspend fun returnAsset(recordId: String, proofImageUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "status" to BorrowStatus.Completed.name,
            "returnDate" to com.google.firebase.Timestamp.now()
        )
        proofImageUrl?.let { updates["proofImageUrl"] = it }
        borrowsCollection.document(recordId).update(updates).await()
    }

    // ── Private mapper helpers ──────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toBorrowRecord(): BorrowRecord? {
        if (!exists()) return null
        return try {
            val rawBorrowDate = get("borrowDate")
            val borrowDateStr = when (rawBorrowDate) {
                is com.google.firebase.Timestamp -> {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    sdf.format(rawBorrowDate.toDate())
                }
                is String -> rawBorrowDate
                else -> ""
            }

            val rawReturnEstimate = get("returnEstimate")
            val returnEstimateStr = when (rawReturnEstimate) {
                is com.google.firebase.Timestamp -> {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    sdf.format(rawReturnEstimate.toDate())
                }
                is String -> rawReturnEstimate
                else -> ""
            }

            val rawReturnDate = get("returnDate")
            val returnDateStr = when (rawReturnDate) {
                is com.google.firebase.Timestamp -> {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    sdf.format(rawReturnDate.toDate())
                }
                is String -> rawReturnDate
                else -> null
            }

            BorrowRecord(
                id = id,
                assetId = getString("assetId") ?: return null,
                assetTitle = getString("assetTitle") ?: "",
                assetStock = getLong("assetStock")?.toInt() ?: 0,
                borrowerId = getString("borrowerId") ?: return null,
                borrowerName = getString("borrowerName") ?: "",
                status = BorrowStatus.valueOf(getString("status") ?: BorrowStatus.Pending.name),
                borrowDate = borrowDateStr,
                returnEstimate = returnEstimateStr,
                returnDate = returnDateStr,
                proofImageUrl = getString("proofImageUrl")
            )
        } catch (e: Exception) {
            null
        }
    }
}
