package dev.stefano.enuventory.domain.repository

import dev.stefano.enuventory.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk semua operasi data yang berhubungan dengan Category.
 *
 * Interface ini ada di domain layer — tidak tahu apakah implementasinya
 * pakai Firestore, Room, atau sumber data lain. Itu urusan data layer.
 */
interface CategoryRepository {

    /** Mengambil semua kategori sebagai stream reaktif. */
    fun getCategories(): Flow<List<Category>>

    /** Menambahkan kategori baru. */
    suspend fun addCategory(name: String)

    /** Memperbarui (rename) kategori yang sudah ada. */
    suspend fun updateCategory(category: Category)

    /** Menghapus kategori berdasarkan ID. */
    suspend fun deleteCategory(categoryId: String)
}
