package dev.stefano.enuventory.ui.screen.user

import androidx.lifecycle.SavedStateHandle
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.usecase.GetRegularUsersUseCase
import dev.stefano.enuventory.domain.usecase.GetUserBorrowHistoryUseCase
import dev.stefano.enuventory.domain.usecase.SetUserDisabledUseCase
import dev.stefano.enuventory.domain.usecase.UpdateUserNameUseCase
import dev.stefano.enuventory.fake.FakeBorrowRepository
import dev.stefano.enuventory.fake.FakeUserRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji [DetailUserAdminViewModel]: profil user, rename, toggle disable, dan riwayat. */
class DetailUserAdminViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepository: FakeUserRepository
    private lateinit var borrowRepository: FakeBorrowRepository

    private val existingUser = User(
        uid = "1",
        name = "Budi Santoso",
        email = "budi@x.com",
        role = UserRole.RegularUser
    )

    private fun createViewModel(userId: String = existingUser.uid) = DetailUserAdminViewModel(
        savedStateHandle = SavedStateHandle(mapOf("userId" to userId)),
        getRegularUsersUseCase = GetRegularUsersUseCase(userRepository),
        getUserBorrowHistoryUseCase = GetUserBorrowHistoryUseCase(borrowRepository),
        updateUserNameUseCase = UpdateUserNameUseCase(userRepository),
        setUserDisabledUseCase = SetUserDisabledUseCase(userRepository)
    )

    @Before
    fun setUp() {
        userRepository = FakeUserRepository()
        borrowRepository = FakeBorrowRepository()
        userRepository.setUsers(listOf(existingUser))
    }

    @Test
    fun `userState reflects the existing user profile`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.userState.collect {} }

            assertEquals(UiState.Success(existingUser), viewModel.userState.value)

            job.cancel()
        }

    @Test
    fun `historyState reflects that user's borrow records only`() =
        runTest(mainDispatcherRule.testDispatcher) {
            borrowRepository.setRecords(
                listOf(
                    BorrowRecord(
                        id = "r1",
                        assetId = "HW-1",
                        assetTitle = "Laptop",
                        borrowerId = "1",
                        borrowerName = "Budi Santoso",
                        status = BorrowStatus.Borrowed,
                        requestedAt = 1_792_195_200_000L,
                        borrowDate = 1_792_195_200_000L,
                        returnEstimate = 1_792_713_600_000L,
                        reason = "Kebutuhan proyek"
                    ),
                    BorrowRecord(
                        id = "r2",
                        assetId = "HW-2",
                        assetTitle = "Mouse",
                        borrowerId = "2",
                        borrowerName = "Someone Else",
                        status = BorrowStatus.Borrowed,
                        requestedAt = 1_792_195_200_000L,
                        borrowDate = 1_792_195_200_000L,
                        returnEstimate = 1_792_713_600_000L,
                        reason = "Kebutuhan proyek"
                    )
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.historyState.collect {} }

            val state = viewModel.historyState.value as UiState.Success
            assertEquals(1, state.data.size)
            assertEquals("r1", state.data.first().id)

            job.cancel()
        }

    @Test
    fun `renameUser persists the trimmed new name`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.renameUser("  Budi Santoso Jr  ") { onSuccessCalled = true }

            assertTrue(onSuccessCalled)
            assertEquals(listOf("1" to "Budi Santoso Jr"), userRepository.updateUserNameCalls)
        }

    @Test
    fun `setDisabled true blocks the account`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.setDisabled(true) { onSuccessCalled = true }

            assertTrue(onSuccessCalled)
            assertEquals(listOf("1" to true), userRepository.setUserDisabledCalls)
        }

    @Test
    fun `userState is Empty when the user id does not exist`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(userId = "does-not-exist")
            val job = launch { viewModel.userState.collect {} }

            assertEquals(UiState.Empty, viewModel.userState.value)
            assertNotNull(viewModel.userState.value)

            job.cancel()
        }
}
