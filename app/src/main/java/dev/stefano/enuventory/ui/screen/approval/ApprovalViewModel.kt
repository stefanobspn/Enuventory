package dev.stefano.enuventory.ui.screen.approval

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.usecase.ApproveRequestUseCase
import dev.stefano.enuventory.domain.usecase.GetPendingRequestsUseCase
import dev.stefano.enuventory.domain.usecase.RejectRequestUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalViewModel @Inject constructor(
    getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val approveRequestUseCase: ApproveRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase
) : ViewModel() {

    val requestsState: StateFlow<UiState<List<BorrowRecord>>> = getPendingRequestsUseCase()
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

    fun approveRequest(recordId: String) {
        viewModelScope.launch {
            approveRequestUseCase(recordId)
        }
    }

    fun rejectRequest(recordId: String) {
        viewModelScope.launch {
            rejectRequestUseCase(recordId)
        }
    }
}
