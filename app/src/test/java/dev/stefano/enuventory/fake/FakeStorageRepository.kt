package dev.stefano.enuventory.fake

import dev.stefano.enuventory.domain.repository.StorageRepository

/** In-memory fake untuk [StorageRepository], dipakai di unit test ViewModel. */
class FakeStorageRepository : StorageRepository {

    var uploadError: Throwable? = null
    val uploadCalls = mutableListOf<Pair<String, ByteArray>>()

    override suspend fun uploadAssetImage(assetId: String, imageBytes: ByteArray): String {
        uploadError?.let { throw it }
        uploadCalls += assetId to imageBytes
        return "https://fake-storage.test/assets/$assetId.jpg"
    }
}
