package dev.stefano.enuventory.ui.screen.user

import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.usecase.CreateUserUseCase
import dev.stefano.enuventory.domain.usecase.GetRegularUsersUseCase
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

/** Menguji [KelolaUserViewModel]: daftar user & alur tambah user baru. */
class KelolaUserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepository: FakeUserRepository

    private fun createViewModel() = KelolaUserViewModel(
        getRegularUsersUseCase = GetRegularUsersUseCase(userRepository),
        createUserUseCase = CreateUserUseCase(userRepository)
    )

    @Before
    fun setUp() {
        userRepository = FakeUserRepository()
    }

    @Test
    fun `usersState reflects the regular users from the repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            userRepository.setUsers(
                listOf(
                    User(
                        uid = "1",
                        name = "Budi",
                        email = "budi@x.com",
                        role = UserRole.RegularUser
                    )
                )
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.usersState.collect {} }

            val state = viewModel.usersState.value as UiState.Success
            assertEquals(1, state.data.size)
            assertEquals("Budi", state.data.first().name)

            job.cancel()
        }

    @Test
    fun `createUser trims name and email before persisting`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.createUser("  Budi  ", "  budi@x.com  ", "password123") {
                onSuccessCalled = true
            }

            assertTrue(onSuccessCalled)
            assertEquals(1, userRepository.createUserCalls.size)
            val (name, email, password) = userRepository.createUserCalls.first()
            assertEquals("Budi", name)
            assertEquals("budi@x.com", email)
            assertEquals("password123", password)
        }

    @Test
    fun `createUser surfaces an error and does not call onSuccess when it fails`() =
        runTest(mainDispatcherRule.testDispatcher) {
            userRepository.createUserError = IllegalStateException("Email sudah dipakai")
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.createUser("Budi", "budi@x.com", "password123") { onSuccessCalled = true }

            assertTrue(onSuccessCalled.not())
            assertNotNull(viewModel.actionError.value)
        }
}
