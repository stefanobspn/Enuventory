package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.AssetRepository
import javax.inject.Inject

/** UseCase untuk menghapus asset. Dipakai di Detail Asset page (Admin). */
class DeleteAssetUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(assetId: String) = assetRepository.deleteAsset(assetId)
}
