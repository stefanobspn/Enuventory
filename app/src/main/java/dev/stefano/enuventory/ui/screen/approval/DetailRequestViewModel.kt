package dev.stefano.enuventory.ui.screen.approval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.usecase.ApproveRequestUseCase
import dev.stefano.enuventory.domain.usecase.CompleteReturnUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetByIdUseCase
import dev.stefano.enuventory.domain.usecase.GetBorrowRecordByIdUseCase
import dev.stefano.enuventory.domain.usecase.RejectRequestUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailRequestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBorrowRecordByIdUseCase: GetBorrowRecordByIdUseCase,
    private val getAssetByIdUseCase: GetAssetByIdUseCase,
    private val approveRequestUseCase: ApproveRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val completeReturnUseCase: CompleteReturnUseCase
) : ViewModel() {

    val recordId: String = savedStateHandle.get<String>("recordId") ?: ""

    private val _uiState = MutableStateFlow<UiState<BorrowRecord>>(UiState.Loading)
    val uiState: StateFlow<UiState<BorrowRecord>> = _uiState.asStateFlow()

    private val _assetImageUrl = MutableStateFlow<String?>(null)
    val assetImageUrl: StateFlow<String?> = _assetImageUrl.asStateFlow()

    init {
        loadRecord()
    }

    fun loadRecord() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val record = getBorrowRecordByIdUseCase(recordId)
                if (record != null) {
                    _uiState.value = UiState.Success(record)
                    _assetImageUrl.value = getAssetByIdUseCase(record.assetId)?.imageUrl
                } else {
                    _uiState.value = UiState.Error("Request tidak ditemukan")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Gagal memuat detail request")
            }
        }
    }

    /**
     * Setujui request + tetapkan jadwal pengambilan. assetId diambil dari record
     * yang sudah dimuat di state (hindari .get() tambahan di alur tulis).
     */
    fun approveRequest(pickupSchedule: Long, onSuccess: () -> Unit) {
        val record = (_uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                approveRequestUseCase(recordId, record.assetId, pickupSchedule)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Gagal menyetujui request")
            }
        }
    }

    fun rejectRequest(rejectionReason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                rejectRequestUseCase(recordId, rejectionReason)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Gagal menolak request")
            }
        }
    }

    /**
     * Proses pengembalian barang oleh admin. Normal → Completed + asset Available,
     * rusak → Damaged + catatan + asset Maintenance. assetId dari record di state.
     */
    fun completeReturn(isDamaged: Boolean, damageNotes: String?, onSuccess: () -> Unit) {
        val record = (_uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                completeReturnUseCase(recordId, record.assetId, isDamaged, damageNotes)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Gagal memproses pengembalian")
            }
        }
    }
}
