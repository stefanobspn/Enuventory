package dev.stefano.enuventory.ui.screen.history

import androidx.lifecycle.SavedStateHandle
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.usecase.GetAssetByIdUseCase
import dev.stefano.enuventory.domain.usecase.GetBorrowRecordByIdUseCase
import dev.stefano.enuventory.fake.FakeAssetRepository
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.pages.DetailRiwayatState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Menguji derivasi status di halaman Detail Riwayat -- ini titik pertemuan alur
 * peminjaman & pengembalian di diagram (dari sini user pencet "Scan QR" saat
 * Menunggu Pengambilan, atau membawa barang ke kantor saat BatasKembali).
 */
class DetailRiwayatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var borrowRepository: FakeBorrowRepository
    private lateinit var assetRepository: FakeAssetRepository

    private fun createViewModel(recordId: String = "record-1") = DetailRiwayatViewModel(
        savedStateHandle = SavedStateHandle(mapOf("recordId" to recordId)),
        getBorrowRecordByIdUseCase = GetBorrowRecordByIdUseCase(borrowRepository),
        getAssetByIdUseCase = GetAssetByIdUseCase(assetRepository)
    )

    private fun recordWith(status: BorrowStatus) = BorrowRecord(
        id = "record-1",
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

    @Before
    fun setUp() {
        borrowRepository = FakeBorrowRepository()
        assetRepository = FakeAssetRepository()
    }

    @Test
    fun `Pending status shows MenungguPersetujuan`() = runTest(mainDispatcherRule.testDispatcher) {
        borrowRepository.setRecords(listOf(recordWith(BorrowStatus.Pending)))
        val viewModel = createViewModel()

        val state = viewModel.uiState.value as UiState.Success
        assertEquals(DetailRiwayatState.MenungguPersetujuan, state.data.riwayatState)
    }

    @Test
    fun `WaitingPickup status shows MenungguPengambilan`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(recordWith(BorrowStatus.WaitingPickup)))
            val viewModel = createViewModel()

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailRiwayatState.MenungguPengambilan, state.data.riwayatState)
        }

    @Test
    fun `Borrowed status shows BatasKembali`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(recordWith(BorrowStatus.Borrowed)))
            val viewModel = createViewModel()

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailRiwayatState.BatasKembali, state.data.riwayatState)
        }

    @Test
    fun `Damaged status shows DikembalikanRusak`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(recordWith(BorrowStatus.Damaged).copy(damageNotes = "Layar retak"))
            )
            val viewModel = createViewModel()

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailRiwayatState.DikembalikanRusak, state.data.riwayatState)
            assertEquals("Layar retak", state.data.record.damageNotes)
        }

    @Test
    fun `Completed status shows Dikembalikan`() = runTest(mainDispatcherRule.testDispatcher) {
        borrowRepository.setRecords(listOf(recordWith(BorrowStatus.Completed)))
        val viewModel = createViewModel()

        val state = viewModel.uiState.value as UiState.Success
        assertEquals(DetailRiwayatState.Dikembalikan, state.data.riwayatState)
    }

    @Test
    fun `Rejected status shows Ditolak`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(recordWith(BorrowStatus.Rejected)))
            val viewModel = createViewModel()

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailRiwayatState.Ditolak, state.data.riwayatState)
        }

    @Test
    fun `record not found shows Error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(recordId = "does-not-exist")
        assertTrue(viewModel.uiState.value is UiState.Error)
    }

    @Test
    fun `uiModel carries the asset's image url for display`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(recordWith(BorrowStatus.Borrowed)))
            assetRepository.setAssets(
                listOf(
                    dev.stefano.enuventory.domain.model.Asset(
                        id = "asset-1",
                        title = "Proyektor Epson",
                        status = dev.stefano.enuventory.domain.model.AssetStatus.Reserved,
                        category = "Elektronik",
                        description = "",
                        imageUrl = "https://example.test/proyektor.jpg"
                    )
                )
            )
            val viewModel = createViewModel()

            val state = viewModel.uiState.value as UiState.Success
            assertEquals("https://example.test/proyektor.jpg", state.data.assetImageUrl)
        }
}
