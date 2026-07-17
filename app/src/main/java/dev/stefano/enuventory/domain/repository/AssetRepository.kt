package dev.stefano.enuventory.domain.repository

import dev.stefano.enuventory.domain.model.Asset
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk semua operasi data yang berhubungan dengan Asset.
 *
 * Interface ini ada di domain layer — tidak tahu apakah implementasinya
 * pakai Firestore, Room, atau sumber data lain. Itu urusan data layer.
 */
interface AssetRepository {

    /**
     * Mengambil semua asset sebagai stream reaktif.
     * UI akan otomatis ter-update saat data berubah di Firestore.
     */
    fun getAssets(): Flow<List<Asset>>

    /**
     * Mengambil satu asset berdasarkan ID-nya.
     * Mengembalikan null jika asset tidak ditemukan.
     */
    suspend fun getAssetById(assetId: String): Asset?

    /** Menambahkan asset baru ke database. */
    suspend fun addAsset(asset: Asset)

    /** Memperbarui data asset yang sudah ada. */
    suspend fun updateAsset(asset: Asset)

    /** Menghapus asset berdasarkan ID. */
    suspend fun deleteAsset(assetId: String)
}
