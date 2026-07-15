package dev.stefano.enuventory.ui.screen.approval

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.usecase.GetAllBorrowRecordsUseCase
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Menguji sisi admin dari langkah 3 di diagram alur peminjaman:
 * "Admin dapat notifikasi, ACC bisa datang ke kantor".
 *
 * Catatan: "dapat notifikasi" di sini cuma berarti realtime listener Firestore
 * yang aktif selama halaman Approval terbuka -- BUKAN push notification asli.
 * ViewModel ini sekarang menyuplai SEMUA record (tab Pending/Aktif/Selesai
 * difilter di page); aksi approve/reject terjadi di DetailRequestViewModel.
 */
class ApprovalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var borrowRepository: FakeBorrowRepository

    private fun createViewModel() = ApprovalViewModel(
        getAllBorrowRecordsUseCase = GetAllBorrowRecordsUseCase(borrowRepository)
    )

    @Before
    fun setUp() {
        borrowRepository = FakeBorrowRepository()
    }

    @Test
    fun `empty pending list shows Empty state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val job = launch { viewModel.requestsState.collect {} }

        assertEquals(UiState.Empty, viewModel.requestsState.value)

        job.cancel()
    }

    @Test
    fun `all records of every status surface for the tabs to filter`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(
                    record(id = "r1", status = BorrowStatus.Pending),
                    record(id = "r2", status = BorrowStatus.Borrowed),
                    record(id = "r3", status = BorrowStatus.WaitingPickup),
                    record(id = "r4", status = BorrowStatus.Rejected),
                    record(id = "r5", status = BorrowStatus.Completed),
                    record(id = "r6", status = BorrowStatus.Damaged)
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.requestsState.collect {} }

            val state = viewModel.requestsState.value as UiState.Success
            assertEquals(
                setOf("r1", "r2", "r3", "r4", "r5", "r6"),
                state.data.map { it.id }.toSet()
            )

            job.cancel()
        }

    private fun record(id: String, status: BorrowStatus) = BorrowRecord(
        id = id,
        assetId = "asset-1",
        assetTitle = "Proyektor Epson",
        borrowerId = "user-1",
        borrowerName = "Budi",
        status = status,
        requestedAt = 1_792_195_200_000L,
        borrowDate = 1_792_195_200_000L,
        returnEstimate = 1_792_713_600_000L,
        reason = "Kebutuhan presentasi"
    )
}
