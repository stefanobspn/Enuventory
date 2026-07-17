package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.repository.AssetRepository
import javax.inject.Inject

/** UseCase untuk menambahkan asset baru. Dipakai di TambahAsset page (Admin). */
class AddAssetUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(asset: Asset) = assetRepository.addAsset(asset)
}
