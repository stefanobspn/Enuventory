package dev.stefano.enuventory.ui.screen.approval

import androidx.lifecycle.SavedStateHandle
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.usecase.ApproveRequestUseCase
import dev.stefano.enuventory.domain.usecase.CompleteReturnUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetByIdUseCase
import dev.stefano.enuventory.domain.usecase.GetBorrowRecordByIdUseCase
import dev.stefano.enuventory.domain.usecase.RejectRequestUseCase
import dev.stefano.enuventory.fake.FakeAssetRepository
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji langkah 3 di diagram: admin membuka detail request lalu ACC (+jadwal)/tolak (+alasan). */
class DetailRequestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var borrowRepository: FakeBorrowRepository
    private lateinit var assetRepository: FakeAssetRepository

    private val pickupSchedule = 1_792_281_600_000L

    private val pendingRecord = BorrowRecord(
        id = "record-1",
        assetId = "asset-1",
        assetTitle = "Proyektor Epson",
        borrowerId = "user-1",
        borrowerName = "Budi",
        status = BorrowStatus.Pending,
        requestedAt = 1_792_195_200_000L,
        borrowDate = 1_792_195_200_000L,
        returnEstimate = 1_792_713_600_000L,
        reason = "Kebutuhan presentasi"
    )

    private val borrowedRecord = pendingRecord.copy(
        status = BorrowStatus.Borrowed,
        pickupSchedule = pickupSchedule,
        pickedUpAt = pickupSchedule
    )

    private fun createViewModel(recordId: String = pendingRecord.id) = DetailRequestViewModel(
        savedStateHandle = SavedStateHandle(mapOf("recordId" to recordId)),
        getBorrowRecordByIdUseCase = GetBorrowRecordByIdUseCase(borrowRepository),
        getAssetByIdUseCase = GetAssetByIdUseCase(assetRepository),
        approveRequestUseCase = ApproveRequestUseCase(borrowRepository),
        rejectRequestUseCase = RejectRequestUseCase(borrowRepository),
        completeReturnUseCase = CompleteReturnUseCase(borrowRepository)
    )

    @Before
    fun setUp() {
        borrowRepository = FakeBorrowRepository().apply { setRecords(listOf(pendingRecord)) }
        assetRepository = FakeAssetRepository()
    }

    @Test
    fun `loadRecord shows Success with the requested record`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val state = viewModel.uiState.value as UiState.Success
            assertEquals(pendingRecord.id, state.data.id)
        }

    @Test
    fun `loadRecord also loads the asset's image url for display`() =
        runTest(mainDispatcherRule.testDispatcher) {
            assetRepository.setAssets(
                listOf(
                    dev.stefano.enuventory.domain.model.Asset(
                        id = pendingRecord.assetId,
                        title = pendingRecord.assetTitle,
                        status = AssetStatus.Reserved,
                        category = "Elektronik",
                        description = "",
                        imageUrl = "https://example.test/proyektor.jpg"
                    )
                )
            )
            val viewModel = createViewModel()

            assertEquals("https://example.test/proyektor.jpg", viewModel.assetImageUrl.value)
        }

    @Test
    fun `loadRecord shows Error when record does not exist`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(recordId = "does-not-exist")
            assertTrue(viewModel.uiState.value is UiState.Error)
        }

    @Test
    fun `approveRequest sets WaitingPickup with schedule and reserves the asset`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.approveRequest(pickupSchedule, onSuccess = { onSuccessCalled = true })

            assertTrue(onSuccessCalled)
            val record = borrowRepository.currentRecords().first()
            assertEquals(BorrowStatus.WaitingPickup, record.status)
            assertEquals(pickupSchedule, record.pickupSchedule)
            assertEquals(
                listOf(pendingRecord.assetId to AssetStatus.Reserved),
                borrowRepository.assetStatusUpdates
            )
        }

    @Test
    fun `rejectRequest sets status to Rejected with the reason and calls onSuccess`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.rejectRequest(
                "Barang dipakai internal",
                onSuccess = { onSuccessCalled = true })

            assertTrue(onSuccessCalled)
            val record = borrowRepository.currentRecords().first()
            assertEquals(BorrowStatus.Rejected, record.status)
            assertEquals("Barang dipakai internal", record.rejectionReason)
        }

    @Test
    fun `completeReturn with Normal condition sets Completed and frees the asset`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(borrowedRecord))
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.completeReturn(
                isDamaged = false,
                damageNotes = null,
                onSuccess = { onSuccessCalled = true })

            assertTrue(onSuccessCalled)
            val record = borrowRepository.currentRecords().first()
            assertEquals(BorrowStatus.Completed, record.status)
            assertEquals(null, record.damageNotes)
            assertEquals(
                listOf(pendingRecord.assetId to AssetStatus.Available),
                borrowRepository.assetStatusUpdates
            )
        }

    @Test
    fun `completeReturn with Rusak condition sets Damaged, notes, and sends asset to Maintenance`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(borrowedRecord))
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.completeReturn(
                isDamaged = true,
                damageNotes = "Layar retak",
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled)
            val record = borrowRepository.currentRecords().first()
            assertEquals(BorrowStatus.Damaged, record.status)
            assertEquals("Layar retak", record.damageNotes)
            assertEquals(
                listOf(pendingRecord.assetId to AssetStatus.Maintenance),
                borrowRepository.assetStatusUpdates
            )
        }

    @Test
    fun `completeReturn failure surfaces an Error state and skips onSuccess`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(listOf(borrowedRecord))
            borrowRepository.completeReturnError = IllegalStateException("Firestore down")
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.completeReturn(
                isDamaged = false,
                damageNotes = null,
                onSuccess = { onSuccessCalled = true })

            assertFalse(onSuccessCalled)
            assertTrue(viewModel.uiState.value is UiState.Error)
        }

    @Test
    fun `approveRequest failure surfaces an Error state and skips onSuccess`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.approveRequestError = IllegalStateException("Firestore down")
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.approveRequest(pickupSchedule, onSuccess = { onSuccessCalled = true })

            assertFalse(onSuccessCalled)
            assertTrue(viewModel.uiState.value is UiState.Error)
        }
}
