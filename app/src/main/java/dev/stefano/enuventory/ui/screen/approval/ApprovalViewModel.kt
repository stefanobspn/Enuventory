package dev.stefano.enuventory.ui.screen.approval

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.usecase.GetAllBorrowRecordsUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ApprovalViewModel @Inject constructor(
    getAllBorrowRecordsUseCase: GetAllBorrowRecordsUseCase
) : ViewModel() {

    // Seluruh record (semua status) — pemisahan tab Pending/Aktif/Selesai terjadi di page.
    val requestsState: StateFlow<UiState<List<BorrowRecord>>> = getAllBorrowRecordsUseCase()
        .map { records ->
            if (records.isEmpty()) UiState.Empty
            else UiState.Success(records)
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Terjadi kesalahan")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )
}
