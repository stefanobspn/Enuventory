package dev.stefano.enuventory.ui.screen.asset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.usecase.DeleteAssetUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailAssetAdminViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAssetsUseCase: GetAssetsUseCase,
    private val deleteAssetUseCase: DeleteAssetUseCase
) : ViewModel() {

    val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    val assetState: StateFlow<UiState<Asset>> = getAssetsUseCase()
        .map { list ->
            val asset = list.find { it.id == assetId }
            if (asset == null) UiState.Empty
            else UiState.Success(asset)
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat detail aset")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun deleteAsset(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                deleteAssetUseCase(assetId)
                onSuccess()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
