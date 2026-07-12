package dev.stefano.enuventory.ui.screen.asset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.domain.usecase.RejectRequestUseCase
import dev.stefano.enuventory.domain.usecase.RequestBorrowUseCase
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.pages.DetailAssetUserState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailAssetUserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAssetsUseCase: GetAssetsUseCase,
    private val getUserBorrowHistoryUseCase: GetUserBorrowHistoryUseCase,
    private val requestBorrowUseCase: RequestBorrowUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase
) : ViewModel() {

    val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<DetailAssetUiModel>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UiState.Error("Sesi tidak ditemukan"))
            } else {
                val assetFlow = getAssetsUseCase().map { list -> list.find { it.id == assetId } }
                assetFlow.combine(getUserBorrowHistoryUseCase(user.uid)) { asset, history ->
                    if (asset == null) {
                        UiState.Error("Aset tidak ditemukan")
                    } else {
                        val activeRecord = history.find { it.assetId == asset.id && !it.isFinished }
                        val relationshipState = when (activeRecord?.status) {
                            BorrowStatus.Pending -> DetailAssetUserState.MenungguPersetujuan
                            BorrowStatus.Borrowed -> DetailAssetUserState.SedangDipinjam
                            else -> DetailAssetUserState.Normal
                        }
                        UiState.Success(
                            DetailAssetUiModel(
                                asset = asset,
                                relationshipState = relationshipState,
                                activeRecordId = activeRecord?.id
                            )
                        )
                    }
                }.catch { e -> emit(UiState.Error(e.message ?: "Terjadi kesalahan")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun requestBorrow(returnEstimate: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().filterNotNull().firstOrNull() ?: return@launch
            try {
                requestBorrowUseCase(assetId, user.uid, returnEstimate)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun cancelBorrow(recordId: String) {
        viewModelScope.launch {
            try {
                rejectRequestUseCase(recordId)
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}

data class DetailAssetUiModel(
    val asset: Asset,
    val relationshipState: DetailAssetUserState,
    val activeRecordId: String? = null
)
