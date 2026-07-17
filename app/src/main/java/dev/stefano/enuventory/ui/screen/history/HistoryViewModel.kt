package dev.stefano.enuventory.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import dev.stefano.enuventory.domain.usecase.GetAssetByIdUseCase

data class HistoryItemUiModel(
    val record: BorrowRecord,
    val imageUrl: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserBorrowHistoryUseCase: GetUserBorrowHistoryUseCase,
    private val getAssetByIdUseCase: GetAssetByIdUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyState: StateFlow<UiState<List<HistoryItemUiModel>>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UiState.Error("Sesi tidak ditemukan"))
            } else {
                getUserBorrowHistoryUseCase(user.uid)
                    .map { records ->
                        if (records.isEmpty()) {
                            UiState.Empty
                        } else {
                            val items = records.map { record ->
                                val asset = getAssetByIdUseCase(record.assetId)
                                HistoryItemUiModel(record, asset?.imageUrl)
                            }
                            UiState.Success(items)
                        }
                    }
                    .catch { e -> emit(UiState.Error(e.message ?: "Terjadi kesalahan")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )
}
