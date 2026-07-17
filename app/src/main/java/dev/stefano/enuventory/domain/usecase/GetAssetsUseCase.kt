package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase untuk mengambil daftar semua asset.
 *
 * Setiap UseCase punya satu tanggung jawab tunggal (Single Responsibility).
 * ViewModel hanya memanggil UseCase, tidak tahu Repository.
 */
class GetAssetsUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    operator fun invoke(): Flow<List<Asset>> = assetRepository.getAssets()
}
