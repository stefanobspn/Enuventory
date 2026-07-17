package dev.stefano.enuventory.ui.screen.asset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.usecase.AddCategoryUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.UpdateAssetUseCase
import dev.stefano.enuventory.domain.usecase.UploadAssetImageUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAssetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAssetsUseCase: GetAssetsUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase,
    private val uploadAssetImageUseCase: UploadAssetImageUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModel() {

    val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    val assetState: StateFlow<UiState<Asset>> = getAssetsUseCase()
        .map { list ->
            val asset = list.find { it.id == assetId }
            if (asset == null) UiState.Empty else UiState.Success(asset)
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat detail aset")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    val categories: StateFlow<List<Category>> = getCategoriesUseCase()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addCategory(name: String, onSuccess: (String) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                addCategoryUseCase(name.trim())
                onSuccess(name.trim())
            } catch (e: Exception) {
                // Kalau gagal, biarkan admin tetap bisa lanjut pakai nama itu untuk asset
                // ini -- kegagalan persist kategori gak boleh mengeblok alur edit asset.
                onSuccess(name.trim())
            }
        }
    }

    fun editAsset(
        title: String,
        statusStr: String,
        category: String,
        description: String,
        imageBytes: ByteArray? = null,
        onSuccess: () -> Unit
    ) {
        val status = when (statusStr) {
            "Tersedia" -> AssetStatus.Available
            "Direservasi" -> AssetStatus.Reserved
            else -> AssetStatus.Maintenance
        }
        val existingImageUrl = (assetState.value as? UiState.Success)?.data?.imageUrl

        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                val imageUrl =
                    imageBytes?.let { uploadAssetImageUseCase(assetId, it) } ?: existingImageUrl
                val asset = Asset(
                    id = assetId,
                    title = title,
                    status = status,
                    category = category.ifBlank { "All" },
                    description = description,
                    imageUrl = imageUrl
                )
                updateAssetUseCase(asset)
                _saveState.value = UiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Gagal memperbarui asset")
            }
        }
    }

    fun resetState() {
        _saveState.value = UiState.Success(Unit)
    }
}
