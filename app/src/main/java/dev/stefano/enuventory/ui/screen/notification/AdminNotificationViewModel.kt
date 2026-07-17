package dev.stefano.enuventory.ui.screen.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.usecase.GetPendingRequestsUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdminNotificationViewModel @Inject constructor(
    getPendingRequestsUseCase: GetPendingRequestsUseCase
) : ViewModel() {

    val notificationsState: StateFlow<UiState<List<NotificationItem>>> = getPendingRequestsUseCase()
        .map { records ->
            if (records.isEmpty()) UiState.Empty
            else UiState.Success(
                records.map { record ->
                    NotificationItem(
                        id = record.id,
                        title = "Permintaan Peminjaman Baru",
                        message = "${record.borrowerName} mengajukan pinjam \"${record.assetTitle}\""
                    )
                }
            )
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat notifikasi")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )
}
