package dev.stefano.enuventory.fake

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory fake untuk [AssetRepository], dipakai di unit test ViewModel. */
class FakeAssetRepository : AssetRepository {

    private val assetsFlow = MutableStateFlow<List<Asset>>(emptyList())

    val addAssetCalls = mutableListOf<Asset>()
    val updateAssetCalls = mutableListOf<Asset>()
    val deleteAssetCalls = mutableListOf<String>()

    /**
     * Buat mensimulasikan ID generator yang bentrok N kali pertama tanpa perlu
     * mengontrol randomness beneran: selama > 0, [getAssetById] akan bilang "sudah dipakai"
     * (return non-null) untuk ID apapun yang ditanyakan, lalu di-decrement.
     */
    var collideForNextNLookups = 0

    fun setAssets(assets: List<Asset>) {
        assetsFlow.value = assets
    }

    override fun getAssets(): Flow<List<Asset>> = assetsFlow

    override suspend fun getAssetById(assetId: String): Asset? {
        if (collideForNextNLookups > 0) {
            collideForNextNLookups--
            return assetsFlow.value.firstOrNull() ?: Asset(
                id = assetId,
                title = "Simulated collision",
                status = dev.stefano.enuventory.domain.model.AssetStatus.Available,
                category = "",
                description = ""
            )
        }
        return assetsFlow.value.find { it.id == assetId }
    }

    override suspend fun addAsset(asset: Asset) {
        addAssetCalls += asset
        assetsFlow.value = assetsFlow.value + asset
    }

    override suspend fun updateAsset(asset: Asset) {
        updateAssetCalls += asset
        assetsFlow.value = assetsFlow.value.map { if (it.id == asset.id) asset else it }
    }

    override suspend fun deleteAsset(assetId: String) {
        deleteAssetCalls += assetId
        assetsFlow.value = assetsFlow.value.filterNot { it.id == assetId }
    }
}
