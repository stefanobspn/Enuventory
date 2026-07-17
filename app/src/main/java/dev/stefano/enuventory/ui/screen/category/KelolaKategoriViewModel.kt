package dev.stefano.enuventory.ui.screen.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.usecase.AddCategoryUseCase
import dev.stefano.enuventory.domain.usecase.DeleteCategoryUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.UpdateAssetUseCase
import dev.stefano.enuventory.domain.usecase.UpdateCategoryUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Kategori beserta jumlah asset yang sedang memakainya, dipakai buat tampilan Kelola Kategori. */
data class CategoryUi(
    val category: Category,
    val usageCount: Int
)

@HiltViewModel
class KelolaKategoriViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAssetsUseCase: GetAssetsUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private var latestAssets: List<Asset> = emptyList()

    val categoriesState: StateFlow<UiState<List<CategoryUi>>> =
        combine(getCategoriesUseCase(), getAssetsUseCase()) { categories, assets ->
            latestAssets = assets
            if (categories.isEmpty()) UiState.Empty
            else UiState.Success(
                categories.map { category ->
                    CategoryUi(
                        category = category,
                        usageCount = assets.count { it.category == category.name }
                    )
                }
            )
        }
            .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat kategori")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            )

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun clearActionError() {
        _actionError.value = null
    }

    fun addCategory(name: String, onSuccess: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                addCategoryUseCase(name.trim())
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Gagal menambahkan kategori"
            }
        }
    }

    fun renameCategory(category: Category, newName: String, onSuccess: () -> Unit) {
        val trimmedName = newName.trim()
        if (trimmedName.isBlank() || trimmedName == category.name) return
        viewModelScope.launch {
            try {
                updateCategoryUseCase(category.copy(name = trimmedName))
                // Cascade: semua asset yang masih pakai nama lama ikut di-update,
                // supaya badge Home & picker kategori gak nyisain data basi.
                latestAssets
                    .filter { it.category == category.name }
                    .forEach { asset -> updateAssetUseCase(asset.copy(category = trimmedName)) }
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Gagal mengubah nama kategori"
            }
        }
    }

    fun deleteCategory(categoryUi: CategoryUi, onSuccess: () -> Unit) {
        if (categoryUi.usageCount > 0) {
            _actionError.value =
                "Kategori \"${categoryUi.category.name}\" masih dipakai ${categoryUi.usageCount} asset, " +
                        "pindahin dulu kategori asset-nya sebelum dihapus."
            return
        }
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryUi.category.id)
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Gagal menghapus kategori"
            }
        }
    }
}
