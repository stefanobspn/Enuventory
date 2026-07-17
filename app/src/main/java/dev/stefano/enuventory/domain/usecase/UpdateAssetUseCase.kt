package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.repository.AssetRepository
import javax.inject.Inject

/** UseCase untuk memperbarui data asset yang sudah ada. */
class UpdateAssetUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(asset: Asset) = assetRepository.updateAsset(asset)
}
