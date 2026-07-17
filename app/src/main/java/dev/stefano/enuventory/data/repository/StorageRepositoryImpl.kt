package dev.stefano.enuventory.data.repository

import dev.stefano.enuventory.domain.repository.StorageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import javax.inject.Inject

private const val ASSETS_BUCKET = "Enuventory"

class StorageRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : StorageRepository {

    override suspend fun uploadAssetImage(assetId: String, imageBytes: ByteArray): String {
        val bucket = supabaseClient.storage.from(ASSETS_BUCKET)
        val path = "$assetId.jpg"
        bucket.upload(path, imageBytes) { upsert = true }
        return bucket.publicUrl(path)
    }
}
