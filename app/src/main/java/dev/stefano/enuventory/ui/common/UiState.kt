package dev.stefano.enuventory.ui.common

/**
 * Representasi generik dari state sebuah screen/komponen UI yang
 * memuat data dari sumber asinkron (network, database, dsb).
 *
 * Menggantikan semua enum class per-page seperti:
 * - HomeUserState { Normal, Loading, Error, Empty }
 * - HistoryPageState { Normal, Loading, Error, Empty }
 * - dsb.
 *
 * Penggunaan:
 * ```
 * val assetsState: StateFlow<UiState<List<Asset>>> = ...
 *
 * when (val state = assetsState) {
 *     is UiState.Loading -> ShowSkeleton()
 *     is UiState.Success -> ShowList(state.data)
 *     is UiState.Error   -> ShowError(state.message)
 *     is UiState.Empty   -> ShowEmptyState()
 * }
 * ```
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
