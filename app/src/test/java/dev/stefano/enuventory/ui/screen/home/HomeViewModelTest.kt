package dev.stefano.enuventory.ui.screen.home

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.GetCurrentUserUseCase
import dev.stefano.enuventory.domain.usecase.GetPendingRequestsUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.fake.FakeAssetRepository
import dev.stefano.enuventory.fake.FakeAuthRepository
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.FakeCategoryRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji [HomeViewModel]: sinkronisasi badge kategori & badge notifikasi (Admin/User). */
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var assetRepository: FakeAssetRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var borrowRepository: FakeBorrowRepository
    private lateinit var authRepository: FakeAuthRepository

    private val hourMillis = 60L * 60 * 1000

    private val currentUser =
        User(uid = "u1", name = "Budi", email = "budi@x.com", role = UserRole.RegularUser)

    private fun createViewModel() = HomeViewModel(
        getAssetsUseCase = GetAssetsUseCase(assetRepository),
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
        getPendingRequestsUseCase = GetPendingRequestsUseCase(borrowRepository),
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
        getUserBorrowHistoryUseCase = GetUserBorrowHistoryUseCase(borrowRepository)
    )

    @Before
    fun setUp() {
        assetRepository = FakeAssetRepository()
        categoryRepository = FakeCategoryRepository()
        borrowRepository = FakeBorrowRepository()
        authRepository = FakeAuthRepository(initialUser = currentUser)
    }

    @Test
    fun `categoriesState defaults to just All when there are no categories yet`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.categoriesState.collect {} }

            assertEquals(listOf("All"), viewModel.categoriesState.value)

            job.cancel()
        }

    @Test
    fun `categoriesState prefixes All before the real managed categories`() =
        runTest(mainDispatcherRule.testDispatcher) {
            categoryRepository.setCategories(
                listOf(
                    Category(id = "1", name = "Elektro"),
                    Category(id = "2", name = "IoT"),
                    Category(id = "3", name = "Mekanik")
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.categoriesState.collect {} }

            assertEquals(
                listOf("All", "Elektro", "IoT", "Mekanik"),
                viewModel.categoriesState.value
            )

            job.cancel()
        }

    @Test
    fun `adminNotificationCount reflects the number of pending requests`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(
                    record(id = "r1", assetId = "HW-1", status = BorrowStatus.Pending),
                    record(id = "r2", assetId = "HW-2", status = BorrowStatus.Pending),
                    record(id = "r3", assetId = "HW-3", status = BorrowStatus.Borrowed)
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.adminNotificationCount.collect {} }

            assertEquals(2, viewModel.adminNotificationCount.value)

            job.cancel()
        }

    @Test
    fun `userNotificationCount counts only that user's near-deadline borrowed records`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val now = System.currentTimeMillis()
            val soon = now + hourMillis // 1 jam lagi
            val far = now + 10 * 24 * hourMillis // 10 hari lagi
            borrowRepository.setRecords(
                listOf(
                    record(
                        id = "r1", assetId = "HW-1",
                        status = BorrowStatus.Borrowed, returnEstimate = soon
                    ),
                    record(
                        id = "r2", assetId = "HW-2",
                        status = BorrowStatus.Borrowed, returnEstimate = far
                    ),
                    record(
                        id = "r3", assetId = "HW-3", borrowerId = "someone-else",
                        status = BorrowStatus.Borrowed, returnEstimate = soon
                    )
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.userNotificationCount.collect {} }

            assertEquals(1, viewModel.userNotificationCount.value)

            job.cancel()
        }

    private fun record(
        id: String,
        assetId: String,
        status: BorrowStatus,
        borrowerId: String = currentUser.uid,
        returnEstimate: Long = System.currentTimeMillis() + 7 * 24 * hourMillis
    ) = BorrowRecord(
        id = id,
        assetId = assetId,
        assetTitle = "Barang $assetId",
        borrowerId = borrowerId,
        borrowerName = "Budi",
        status = status,
        requestedAt = System.currentTimeMillis() - 24 * hourMillis,
        borrowDate = System.currentTimeMillis() - 24 * hourMillis,
        returnEstimate = returnEstimate,
        reason = "Kebutuhan proyek"
    )
}
