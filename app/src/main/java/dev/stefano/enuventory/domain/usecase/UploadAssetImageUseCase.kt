package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.StorageRepository
import javax.inject.Inject

/** UseCase untuk upload foto asset. Dipakai di TambahAsset page (Admin). */
class UploadAssetImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(assetId: String, imageBytes: ByteArray): String =
        storageRepository.uploadAssetImage(assetId, imageBytes)
}
