package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.repository.AssetRepository
import javax.inject.Inject

/** UseCase untuk mengambil detail satu asset berdasarkan ID. */
class GetAssetByIdUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(assetId: String): Asset? =
        assetRepository.getAssetById(assetId)
}
