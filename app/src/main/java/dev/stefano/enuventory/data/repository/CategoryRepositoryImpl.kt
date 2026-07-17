package dev.stefano.enuventory.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.repository.CategoryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    // Nama collection di Firestore
    private val categoriesCollection = firestore.collection("categories")

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        // addSnapshotListener membuat stream reaktif — UI update otomatis saat data berubah
        val listener = categoriesCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val categories = snapshot.documents
                .mapNotNull { doc -> doc.toCategory() }
                // Sorted client-side (bukan Firestore .orderBy()) supaya gak butuh
                // composite index — pola yang sama dipakai di AssetRepositoryImpl.
                .sortedBy { it.name.lowercase() }
            trySend(categories)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addCategory(name: String) {
        categoriesCollection.document().set(mapOf("name" to name)).await()
    }

    override suspend fun updateCategory(category: Category) {
        categoriesCollection.document(category.id).update("name", category.name).await()
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoriesCollection.document(categoryId).delete().await()
    }

    // ── Private mapper helpers ──────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toCategory(): Category? {
        if (!exists()) return null
        return try {
            Category(
                id = id,
                name = getString("name") ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }
}
