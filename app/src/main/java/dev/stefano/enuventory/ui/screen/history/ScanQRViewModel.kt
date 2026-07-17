package dev.stefano.enuventory.ui.screen.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.usecase.ConfirmPickupUseCase
import dev.stefano.enuventory.domain.usecase.GetBorrowRecordByIdUseCase
import dev.stefano.enuventory.domain.util.AssetQrContent
import dev.stefano.enuventory.ui.pages.ScanQRUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Konfirmasi pengambilan barang: user scan QR yang ditempel admin di barang, hasilnya
 * dicocokkan dengan [expectedAssetId] dari record yang sedang MenungguPengambilan.
 */
@HiltViewModel
class ScanQRViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBorrowRecordByIdUseCase: GetBorrowRecordByIdUseCase,
    private val confirmPickupUseCase: ConfirmPickupUseCase
) : ViewModel() {

    private val recordId: String = savedStateHandle.get<String>("recordId") ?: ""
    private val expectedAssetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val _uiState = MutableStateFlow(ScanQRUiState.Scanning)
    val uiState: StateFlow<ScanQRUiState> = _uiState.asStateFlow()

    private val _assetTitle = MutableStateFlow("")
    val assetTitle: StateFlow<String> = _assetTitle.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Cegah analyzer nge-trigger ulang selagi hasil scan sebelumnya masih diproses/ditampilkan.
    private var isProcessingScan = false

    init {
        viewModelScope.launch {
            _assetTitle.value = getBorrowRecordByIdUseCase(recordId)?.assetTitle ?: ""
        }
    }

    fun onQrDetected(scannedText: String) {
        if (isProcessingScan || _uiState.value != ScanQRUiState.Scanning) return
        isProcessingScan = true
        // Terima format ter-encode (AssetQrContent) atau plain assetId lama sebagai fallback.
        val scannedAssetId = AssetQrContent.decode(scannedText) ?: scannedText
        _uiState.value = if (scannedAssetId == expectedAssetId) {
            ScanQRUiState.Confirming
        } else {
            ScanQRUiState.Mismatch
        }
    }

    fun onConfirmClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ScanQRUiState.Submitting
            try {
                confirmPickupUseCase(recordId)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal konfirmasi pengambilan"
                _uiState.value = ScanQRUiState.Error
            }
        }
    }

    fun onCancelConfirm() {
        isProcessingScan = false
        _uiState.value = ScanQRUiState.Scanning
    }

    fun onUlangiClick() {
        isProcessingScan = false
        _errorMessage.value = null
        _uiState.value = ScanQRUiState.Scanning
    }
}
