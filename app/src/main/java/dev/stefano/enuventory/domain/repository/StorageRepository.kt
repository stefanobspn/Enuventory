package dev.stefano.enuventory.domain.repository

/**
 * Kontrak untuk upload file (mis. foto asset) ke storage.
 * Implementasinya pakai Firebase Storage.
 */
interface StorageRepository {

    /**
     * Upload byte gambar untuk sebuah asset dan mengembalikan download URL-nya.
     */
    suspend fun uploadAssetImage(assetId: String, imageBytes: ByteArray): String
}
