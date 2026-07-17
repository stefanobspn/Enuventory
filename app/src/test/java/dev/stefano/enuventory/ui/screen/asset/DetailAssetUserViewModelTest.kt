package dev.stefano.enuventory.ui.screen.asset

import androidx.lifecycle.SavedStateHandle
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.domain.usecase.RejectRequestUseCase
import dev.stefano.enuventory.domain.usecase.RequestBorrowUseCase
import dev.stefano.enuventory.fake.FakeAssetRepository
import dev.stefano.enuventory.fake.FakeAuthRepository
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.pages.DetailAssetUserState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Menguji alur PEMINJAMAN dari sisi user: langkah 1-2 di diagram
 * ("User see what's available" -> "User checkout").
 *
 * Langkah 3 (admin ACC) & 4 (scan QR) ada di DetailRequestViewModelTest dan
 * dokumen gap terpisah, karena scan QR belum wired ke logic apapun.
 */
class DetailAssetUserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val asset = Asset(
        id = "asset-1",
        title = "Proyektor Epson",
        status = AssetStatus.Available,
        category = "Elektronik",
        description = "Proyektor buat presentasi"
    )
    private val user =
        User(uid = "user-1", name = "Budi", email = "budi@enu.dev", role = UserRole.RegularUser)

    private lateinit var assetRepository: FakeAssetRepository
    private lateinit var borrowRepository: FakeBorrowRepository
    private lateinit var authRepository: FakeAuthRepository

    private fun createViewModel(assetId: String = asset.id): DetailAssetUserViewModel {
        return DetailAssetUserViewModel(
            savedStateHandle = SavedStateHandle(mapOf("assetId" to assetId)),
            getAssetsUseCase = GetAssetsUseCase(assetRepository),
            getUserBorrowHistoryUseCase = GetUserBorrowHistoryUseCase(borrowRepository),
            requestBorrowUseCase = RequestBorrowUseCase(borrowRepository),
            getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
            rejectRequestUseCase = RejectRequestUseCase(borrowRepository)
        )
    }

    @Before
    fun setUp() {
        assetRepository = FakeAssetRepository().apply { setAssets(listOf(asset)) }
        borrowRepository = FakeBorrowRepository()
        authRepository = FakeAuthRepository(initialUser = user)
    }

    @Test
    fun `no active borrow record shows Normal state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.Normal, state.data.relationshipState)
            assertEquals(null, state.data.activeRecordId)

            job.cancel()
        }

    @Test
    fun `pending borrow record shows MenungguPersetujuan state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(pendingRecordFor(asset, user))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.MenungguPersetujuan, state.data.relationshipState)

            job.cancel()
        }

    @Test
    fun `borrowed record shows SedangDipinjam state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(pendingRecordFor(asset, user).copy(status = BorrowStatus.Borrowed))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.SedangDipinjam, state.data.relationshipState)

            job.cancel()
        }

    @Test
    fun `waiting pickup record shows MenungguPengambilan state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(pendingRecordFor(asset, user).copy(status = BorrowStatus.WaitingPickup))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.MenungguPengambilan, state.data.relationshipState)

            job.cancel()
        }

    @Test
    fun `asset not Available without own active record shows TidakTersedia state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            assetRepository.setAssets(listOf(asset.copy(status = AssetStatus.Reserved)))
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.TidakTersedia, state.data.relationshipState)

            job.cancel()
        }

    @Test
    fun `finished record (completed) does not block borrowing again - shows Normal`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(pendingRecordFor(asset, user).copy(status = BorrowStatus.Completed))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.Normal, state.data.relationshipState)

            job.cancel()
        }

    @Test
    fun `finished record (rejected) does not block borrowing again - shows Normal`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(pendingRecordFor(asset, user).copy(status = BorrowStatus.Rejected))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value as UiState.Success
            assertEquals(DetailAssetUserState.Normal, state.data.relationshipState)

            job.cancel()
        }

    @Test
    fun `asset not found shows Error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(assetId = "asset-does-not-exist")
        val job = launch { viewModel.uiState.collect {} }

        assertTrue(viewModel.uiState.value is UiState.Error)

        job.cancel()
    }

    @Test
    fun `no logged in user shows session error`() = runTest(mainDispatcherRule.testDispatcher) {
        authRepository.setUser(null)
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value as UiState.Error
        assertEquals("Sesi tidak ditemukan", state.message)

        job.cancel()
    }

    @Test
    fun `requestBorrow creates a Pending record via repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.uiState.collect {} }

            viewModel.requestBorrow(
                borrowDate = 1_792_195_200_000L,
                returnEstimate = 1_792_713_600_000L,
                reason = "Kebutuhan presentasi"
            )

            val records = borrowRepository.currentRecords()
            assertEquals(1, records.size)
            assertEquals(BorrowStatus.Pending, records.first().status)
            assertEquals(asset.id, records.first().assetId)
            assertEquals(user.uid, records.first().borrowerId)
            assertEquals(1_792_195_200_000L, records.first().borrowDate)
            assertEquals(1_792_713_600_000L, records.first().returnEstimate)
            assertEquals("Kebutuhan presentasi", records.first().reason)

            job.cancel()
        }

    @Test
    fun `cancelBorrow rejects the active request`() = runTest(mainDispatcherRule.testDispatcher) {
        val record = pendingRecordFor(asset, user)
        borrowRepository.setRecords(listOf(record))
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }

        viewModel.cancelBorrow(record.id)

        val cancelled = borrowRepository.currentRecords().first()
        assertEquals(BorrowStatus.Rejected, cancelled.status)
        assertEquals(
            DetailAssetUserViewModel.CANCELLED_BY_BORROWER_REASON,
            cancelled.rejectionReason
        )

        job.cancel()
    }

    private fun pendingRecordFor(asset: Asset, user: User) = BorrowRecord(
        id = "record-1",
        assetId = asset.id,
        assetTitle = asset.title,
        borrowerId = user.uid,
        borrowerName = user.name,
        status = BorrowStatus.Pending,
        requestedAt = 1_792_195_200_000L,
        borrowDate = 1_792_195_200_000L,
        returnEstimate = 1_792_713_600_000L,
        reason = "Kebutuhan presentasi"
    )
}
