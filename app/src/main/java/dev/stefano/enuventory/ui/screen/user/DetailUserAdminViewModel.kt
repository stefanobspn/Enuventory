package dev.stefano.enuventory.ui.screen.user

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.usecase.GetRegularUsersUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.domain.usecase.SetUserDisabledUseCase
import dev.stefano.enuventory.domain.usecase.UpdateUserNameUseCase
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailUserAdminViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRegularUsersUseCase: GetRegularUsersUseCase,
    private val getUserBorrowHistoryUseCase: GetUserBorrowHistoryUseCase,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
    private val setUserDisabledUseCase: SetUserDisabledUseCase
) : ViewModel() {

    val userId: String = savedStateHandle.get<String>("userId") ?: ""

    val userState: StateFlow<UiState<User>> = getRegularUsersUseCase()
        .map { users ->
            val user = users.find { it.uid == userId }
            if (user == null) UiState.Empty else UiState.Success(user)
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat data user")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    val historyState: StateFlow<UiState<List<BorrowRecord>>> = getUserBorrowHistoryUseCase(userId)
        .map { records -> if (records.isEmpty()) UiState.Empty else UiState.Success(records) }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat riwayat")) }
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

    fun renameUser(name: String, onSuccess: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                updateUserNameUseCase(userId, name.trim())
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Gagal mengubah nama user"
            }
        }
    }

    fun setDisabled(disabled: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                setUserDisabledUseCase(userId, disabled)
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Gagal memperbarui status user"
            }
        }
    }
}
