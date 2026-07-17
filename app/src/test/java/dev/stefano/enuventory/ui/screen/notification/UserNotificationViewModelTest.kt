package dev.stefano.enuventory.ui.screen.notification

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.fake.FakeAuthRepository
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji [UserNotificationViewModel]: notifikasi in-app buat batas kembali yang mendekat. */
class UserNotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var borrowRepository: FakeBorrowRepository
    private lateinit var authRepository: FakeAuthRepository

    private val currentUser =
        User(uid = "u1", name = "Budi", email = "budi@x.com", role = UserRole.RegularUser)

    private fun createViewModel() = UserNotificationViewModel(
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
        getUserBorrowHistoryUseCase = GetUserBorrowHistoryUseCase(borrowRepository)
    )

    @Before
    fun setUp() {
        borrowRepository = FakeBorrowRepository()
        authRepository = FakeAuthRepository(initialUser = currentUser)
    }

    @Test
    fun `notificationsState is Empty when nothing is near its deadline`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.notificationsState.collect {} }

            assertEquals(UiState.Empty, viewModel.notificationsState.value)

            job.cancel()
        }

    @Test
    fun `notificationsState surfaces a record whose deadline is near`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val now = System.currentTimeMillis()
            val soon = now + 60 * 60 * 1000
            borrowRepository.setRecords(
                listOf(
                    BorrowRecord(
                        id = "r1",
                        assetId = "HW-1",
                        assetTitle = "Macbook Pro 14",
                        borrowerId = "u1",
                        borrowerName = "Budi",
                        status = BorrowStatus.Borrowed,
                        requestedAt = now - 24 * 60 * 60 * 1000,
                        borrowDate = now - 24 * 60 * 60 * 1000,
                        returnEstimate = soon,
                        reason = "Kebutuhan proyek"
                    )
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.notificationsState.collect {} }

            val state = viewModel.notificationsState.value as UiState.Success
            assertEquals(1, state.data.size)
            assertTrue(state.data.first().message.contains("Macbook Pro 14"))

            job.cancel()
        }

    @Test
    fun `notifications are ordered Terlambat, near-deadline, Jadwal Pengambilan, Ditolak`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val now = System.currentTimeMillis()
            val hourMillis = 60 * 60 * 1000L
            borrowRepository.setRecords(
                listOf(
                    baseRecord(
                        id = "overdue",
                        assetTitle = "Overdue Item",
                        status = BorrowStatus.Borrowed,
                        returnEstimate = now - hourMillis
                    ),
                    baseRecord(
                        id = "upcoming",
                        assetTitle = "Upcoming Item",
                        status = BorrowStatus.Borrowed,
                        returnEstimate = now + hourMillis
                    ),
                    baseRecord(
                        id = "waiting",
                        assetTitle = "Waiting Item",
                        status = BorrowStatus.WaitingPickup,
                        returnEstimate = now + 30 * 24 * hourMillis
                    ).copy(pickupSchedule = now + hourMillis),
                    baseRecord(
                        id = "rejected",
                        assetTitle = "Rejected Item",
                        status = BorrowStatus.Rejected,
                        returnEstimate = now + 30 * 24 * hourMillis
                    ).copy(rejectionReason = "Barang dipakai internal")
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.notificationsState.collect {} }

            val state = viewModel.notificationsState.value as UiState.Success
            assertEquals(
                listOf("overdue", "upcoming", "waiting", "rejected"),
                state.data.map { it.id }
            )
            assertEquals("Melewati Batas Pengembalian", state.data[0].title)
            assertEquals("Batas Pengembalian Segera", state.data[1].title)

            job.cancel()
        }

    private fun baseRecord(
        id: String,
        assetTitle: String,
        status: BorrowStatus,
        returnEstimate: Long
    ) = BorrowRecord(
        id = id,
        assetId = "HW-$id",
        assetTitle = assetTitle,
        borrowerId = currentUser.uid,
        borrowerName = currentUser.name,
        status = status,
        requestedAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
        borrowDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
        returnEstimate = returnEstimate,
        reason = "Kebutuhan proyek"
    )
}
