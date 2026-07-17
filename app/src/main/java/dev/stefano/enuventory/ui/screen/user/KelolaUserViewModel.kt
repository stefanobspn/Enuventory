package dev.stefano.enuventory.ui.screen.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.usecase.CreateUserUseCase
import dev.stefano.enuventory.domain.usecase.GetRegularUsersUseCase
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
class KelolaUserViewModel @Inject constructor(
    private val getRegularUsersUseCase: GetRegularUsersUseCase,
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    val usersState: StateFlow<UiState<List<User>>> = getRegularUsersUseCase()
        .map { users -> if (users.isEmpty()) UiState.Empty else UiState.Success(users) }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat daftar user")) }
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

    fun createUser(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                createUserUseCase(name.trim(), email.trim(), password)
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Gagal membuat akun user"
            }
        }
    }
}
