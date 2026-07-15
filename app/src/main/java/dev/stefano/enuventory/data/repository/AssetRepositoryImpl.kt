package dev.stefano.enuventory.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.repository.AssetRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AssetRepository {

    // Nama collection di Firestore
    private val assetsCollection = firestore.collection("assets")

    override fun getAssets(): Flow<List<Asset>> = callbackFlow {
        // addSnapshotListener membuat stream reaktif — UI update otomatis saat data berubah
        val listener = assetsCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val assets = snapshot.documents.mapNotNull { doc ->
                doc.toAsset()
            }
            trySend(assets)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getAssetById(assetId: String): Asset? {
        return assetsCollection.document(assetId).get().await().toAsset()
    }

    override suspend fun addAsset(asset: Asset) {
        assetsCollection.document(asset.id).set(asset.toMap()).await()
    }

    override suspend fun updateAsset(asset: Asset) {
        assetsCollection.document(asset.id).set(asset.toMap()).await()
    }

    override suspend fun deleteAsset(assetId: String) {
        assetsCollection.document(assetId).delete().await()
    }

    // ── Private mapper helpers ──────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toAsset(): Asset? {
        if (!exists()) return null
        return try {
            Asset(
                id = id,
                title = getString("title") ?: return null,
                status = AssetStatus.fromRaw(getString("status")),
                category = getString("category") ?: "",
                description = getString("description") ?: "",
                imageUrl = getString("imageUrl")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Asset.toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "status" to status.name,
        "category" to category,
        "description" to description,
        "imageUrl" to imageUrl
    )
}
