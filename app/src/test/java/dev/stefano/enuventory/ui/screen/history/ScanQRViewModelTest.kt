package dev.stefano.enuventory.ui.screen.history

import androidx.lifecycle.SavedStateHandle
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.usecase.ConfirmPickupUseCase
import dev.stefano.enuventory.domain.usecase.GetBorrowRecordByIdUseCase
import dev.stefano.enuventory.domain.util.AssetQrContent
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.pages.ScanQRUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji langkah 4 di diagram: konfirmasi pengambilan lewat scan QR (match/mismatch/error). */
class ScanQRViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var borrowRepository: FakeBorrowRepository

    private val record = BorrowRecord(
        id = "record-1",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "user-1",
        borrowerName = "Budi",
        status = BorrowStatus.WaitingPickup,
        requestedAt = 1_792_195_200_000L,
        borrowDate = 1_792_195_200_000L,
        returnEstimate = 1_792_713_600_000L,
        reason = "Kebutuhan presentasi",
        pickupSchedule = 1_792_281_600_000L
    )

    private fun createViewModel() = ScanQRViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("recordId" to record.id, "assetId" to record.assetId)
        ),
        getBorrowRecordByIdUseCase = GetBorrowRecordByIdUseCase(borrowRepository),
        confirmPickupUseCase = ConfirmPickupUseCase(borrowRepository)
    )

    @Before
    fun setUp() {
        borrowRepository = FakeBorrowRepository().apply { setRecords(listOf(record)) }
    }

    @Test
    fun `matching scan of a plain legacy assetId moves to Confirming state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onQrDetected(record.assetId)

            assertEquals(ScanQRUiState.Confirming, viewModel.uiState.value)
        }

    @Test
    fun `matching scan of the encoded AssetQrContent scheme moves to Confirming state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onQrDetected(AssetQrContent.encode(record.assetId))

            assertEquals(ScanQRUiState.Confirming, viewModel.uiState.value)
        }

    @Test
    fun `mismatched scan moves to Mismatch state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onQrDetected("some-other-asset")

        assertEquals(ScanQRUiState.Mismatch, viewModel.uiState.value)
    }

    @Test
    fun `repeated scans while already resolved are ignored`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onQrDetected(record.assetId)
            viewModel.onQrDetected("some-other-asset")

            assertEquals(ScanQRUiState.Confirming, viewModel.uiState.value)
        }

    @Test
    fun `onUlangiClick resets Mismatch back to Scanning`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onQrDetected("some-other-asset")

            viewModel.onUlangiClick()

            assertEquals(ScanQRUiState.Scanning, viewModel.uiState.value)
        }

    @Test
    fun `onCancelConfirm resets Confirming back to Scanning`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onQrDetected(record.assetId)

            viewModel.onCancelConfirm()

            assertEquals(ScanQRUiState.Scanning, viewModel.uiState.value)
        }

    @Test
    fun `onConfirmClick confirms pickup and calls onSuccess`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onQrDetected(record.assetId)
            var onSuccessCalled = false

            viewModel.onConfirmClick(onSuccess = { onSuccessCalled = true })

            assertTrue(onSuccessCalled)
            val updated = borrowRepository.currentRecords().first()
            assertEquals(BorrowStatus.Borrowed, updated.status)
            assertTrue(updated.pickedUpAt != null)
        }

    @Test
    fun `onConfirmClick failure surfaces Error state with a message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.confirmPickupError = IllegalStateException("Firestore down")
            val viewModel = createViewModel()
            viewModel.onQrDetected(record.assetId)
            var onSuccessCalled = false

            viewModel.onConfirmClick(onSuccess = { onSuccessCalled = true })

            assertTrue(onSuccessCalled.not())
            assertEquals(ScanQRUiState.Error, viewModel.uiState.value)
            assertEquals("Firestore down", viewModel.errorMessage.value)
        }

    @Test
    fun `assetTitle is loaded from the record for the confirmation dialog`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(record.assetTitle, viewModel.assetTitle.value)
        }
}
