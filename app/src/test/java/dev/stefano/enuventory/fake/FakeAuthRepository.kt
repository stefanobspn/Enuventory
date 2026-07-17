package dev.stefano.enuventory.fake

import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory fake untuk [AuthRepository]. */
class FakeAuthRepository(initialUser: User? = null) : AuthRepository {

    private val userFlow = MutableStateFlow(initialUser)

    var signInError: Throwable? = null
    var signOutCalled = false

    fun setUser(user: User?) {
        userFlow.value = user
    }

    override fun getCurrentUser(): Flow<User?> = userFlow

    override suspend fun signIn(email: String, password: String) {
        signInError?.let { throw it }
    }

    override suspend fun signOut() {
        signOutCalled = true
        userFlow.value = null
    }
}
