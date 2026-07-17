package dev.stefano.enuventory.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetPendingRequestsUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.util.nearDeadlineRecords
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val ALL_CATEGORIES_LABEL = "All"

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAssetsUseCase: GetAssetsUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    getPendingRequestsUseCase: GetPendingRequestsUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
    getUserBorrowHistoryUseCase: GetUserBorrowHistoryUseCase
) : ViewModel() {

    val assetsState: StateFlow<UiState<List<Asset>>> = getAssetsUseCase()
        .map { assets ->
            if (assets.isEmpty()) UiState.Empty
            else UiState.Success(assets)
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Terjadi kesalahan")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    // Badge filter kategori di Home -- disinkronkan dengan kategori beneran yang
    // dikelola lewat layar Kelola Kategori, bukan daftar hardcoded.
    val categoriesState: StateFlow<List<String>> = getCategoriesUseCase()
        .map { categories -> listOf(ALL_CATEGORIES_LABEL) + categories.map(Category::name) }
        .catch { emit(listOf(ALL_CATEGORIES_LABEL)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf(ALL_CATEGORIES_LABEL)
        )

    // Badge notifikasi bell icon (Admin) -- jumlah request peminjaman yang belum di-approve/tolak.
    val adminNotificationCount: StateFlow<Int> = getPendingRequestsUseCase()
        .map { it.size }
        .catch { emit(0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    // Badge notifikasi bell icon (User) -- jumlah pinjaman aktif yang batas kembalinya
    // udah dekat/lewat, lihat NotificationMapper.nearDeadlineRecords().
    @OptIn(ExperimentalCoroutinesApi::class)
    val userNotificationCount: StateFlow<Int> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else getUserBorrowHistoryUseCase(user.uid)
        }
        .map { records -> records.nearDeadlineRecords().size }
        .catch { emit(0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
}
