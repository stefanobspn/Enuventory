package dev.stefano.enuventory.ui.screen.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.screen.asset.DetailAssetUserViewModel
import dev.stefano.enuventory.ui.util.deadlineMessage
import dev.stefano.enuventory.ui.util.formatDateTime
import dev.stefano.enuventory.ui.util.nearDeadlineRecords
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Notifikasi in-app untuk user — semuanya derived dari record peminjaman miliknya
 * (tanpa koleksi Firestore baru). Urutan prioritas: terlambat/near-deadline dulu,
 * lalu jadwal pengambilan, terakhir penolakan.
 */
@HiltViewModel
class UserNotificationViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    getUserBorrowHistoryUseCase: GetUserBorrowHistoryUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val notificationsState: StateFlow<UiState<List<NotificationItem>>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else getUserBorrowHistoryUseCase(user.uid)
        }
        .map { records ->
            val notifications = buildNotifications(records)
            if (notifications.isEmpty()) UiState.Empty
            else UiState.Success(notifications)
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Gagal memuat notifikasi")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    /**
     * `NotificationItem.id` dipakai langsung sebagai `recordId` saat notifikasi di-tap
     * (navigasi ke Detail Riwayat) -- HARUS tetap `record.id` mentah, bukan diberi
     * prefix, walau ada beberapa kategori di bawah. Ini aman karena kategorinya saling
     * eksklusif lewat status record (Borrowed vs WaitingPickup vs Rejected), jadi satu
     * record gak pernah muncul di lebih dari satu kategori sekaligus.
     */
    private fun buildNotifications(records: List<BorrowRecord>): List<NotificationItem> {
        val nowMillis = System.currentTimeMillis()
        val nearDeadline = records.nearDeadlineRecords(nowMillis = nowMillis)

        // Terlambat diprioritaskan di atas yang cuma mendekati -- keduanya sama-sama
        // "near deadline" tapi urgensinya beda.
        val overdue = nearDeadline
            .filter { it.isOverdue(nowMillis) }
            .sortedBy { it.returnEstimate }
            .map { record ->
                NotificationItem(
                    id = record.id,
                    title = "Melewati Batas Pengembalian",
                    message = "\"${record.assetTitle}\" -- ${record.deadlineMessage(nowMillis)}"
                )
            }

        val upcoming = nearDeadline
            .filterNot { it.isOverdue(nowMillis) }
            .sortedBy { it.returnEstimate }
            .map { record ->
                NotificationItem(
                    id = record.id,
                    title = "Batas Pengembalian Segera",
                    message = "\"${record.assetTitle}\" -- ${record.deadlineMessage(nowMillis)}"
                )
            }

        val pickupSchedules = records
            .filter { it.status == BorrowStatus.WaitingPickup && it.pickupSchedule != null }
            .sortedBy { it.pickupSchedule }
            .map { record ->
                NotificationItem(
                    id = record.id,
                    title = "Jadwal Pengambilan",
                    message = "\"${record.assetTitle}\" -- ambil di kantor pada " +
                            formatDateTime(record.pickupSchedule ?: 0L)
                )
            }

        // Request yang dibatalkan user sendiri tidak perlu dinotifikasi sebagai "Ditolak".
        val rejections = records
            .filter {
                it.status == BorrowStatus.Rejected &&
                        it.rejectionReason != null &&
                        it.rejectionReason != DetailAssetUserViewModel.CANCELLED_BY_BORROWER_REASON
            }
            .sortedByDescending { it.requestedAt }
            .map { record ->
                NotificationItem(
                    id = record.id,
                    title = "Peminjaman Ditolak",
                    message = "\"${record.assetTitle}\" -- ${record.rejectionReason}"
                )
            }

        return overdue + upcoming + pickupSchedules + rejections
    }
}
