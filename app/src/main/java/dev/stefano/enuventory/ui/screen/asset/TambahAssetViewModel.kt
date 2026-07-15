package dev.stefano.enuventory.ui.screen.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.usecase.AddAssetUseCase
import dev.stefano.enuventory.domain.usecase.AddCategoryUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetByIdUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.UploadAssetImageUseCase
import dev.stefano.enuventory.domain.util.AssetIdGenerator
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TambahAssetViewModel @Inject constructor(
    private val addAssetUseCase: AddAssetUseCase,
    private val getAssetByIdUseCase: GetAssetByIdUseCase,
    private val uploadAssetImageUseCase: UploadAssetImageUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModel() {

    private companion object {
        const val MAX_ID_GENERATION_ATTEMPTS = 5
    }

    private val _addState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val addState: StateFlow<UiState<Unit>> = _addState.asStateFlow()

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
                // ini -- kegagalan persist kategori gak boleh mengeblok alur tambah asset.
                onSuccess(name.trim())
            }
        }
    }

    fun addAsset(
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

        viewModelScope.launch {
            _addState.value = UiState.Loading
            try {
                val assetId = generateUniqueAssetId()
                val imageUrl = imageBytes?.let { uploadAssetImageUseCase(assetId, it) }
                val asset = Asset(
                    id = assetId,
                    title = title,
                    status = status,
                    category = category.ifBlank { "All" },
                    description = description,
                    imageUrl = imageUrl
                )
                addAssetUseCase(asset)
                _addState.value = UiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                _addState.value = UiState.Error(e.message ?: "Gagal menambahkan asset")
            }
        }
    }

    fun resetState() {
        _addState.value = UiState.Success(Unit)
    }

    /**
     * Generate ID lalu cek ke repository apakah sudah dipakai asset lain -- perlu karena
     * `AssetRepositoryImpl.addAsset()` pakai `.set()` yang diam-diam overwrite kalau ID bentrok.
     */
    private suspend fun generateUniqueAssetId(): String {
        repeat(MAX_ID_GENERATION_ATTEMPTS) {
            val candidate = AssetIdGenerator.generate()
            if (getAssetByIdUseCase(candidate) == null) return candidate
        }
        throw IllegalStateException("Gagal membuat ID asset yang unik, coba lagi")
    }
}
