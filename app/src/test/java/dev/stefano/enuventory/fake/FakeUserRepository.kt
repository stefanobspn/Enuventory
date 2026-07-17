package dev.stefano.enuventory.fake

import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory fake untuk [UserRepository], dipakai di unit test ViewModel. */
class FakeUserRepository : UserRepository {

    private val usersFlow = MutableStateFlow<List<User>>(emptyList())
    private var nextId = 1

    val createUserCalls = mutableListOf<Triple<String, String, String>>()
    val updateUserNameCalls = mutableListOf<Pair<String, String>>()
    val setUserDisabledCalls = mutableListOf<Pair<String, Boolean>>()

    var createUserError: Exception? = null

    fun setUsers(users: List<User>) {
        usersFlow.value = users
    }

    override fun getRegularUsers(): Flow<List<User>> = usersFlow

    override suspend fun createUser(name: String, email: String, password: String): User {
        createUserCalls += Triple(name, email, password)
        createUserError?.let { throw it }
        val user =
            User(uid = "user-${nextId++}", name = name, email = email, role = UserRole.RegularUser)
        usersFlow.value = usersFlow.value + user
        return user
    }

    override suspend fun updateUserName(uid: String, name: String) {
        updateUserNameCalls += uid to name
        usersFlow.value = usersFlow.value.map { if (it.uid == uid) it.copy(name = name) else it }
    }

    override suspend fun setUserDisabled(uid: String, disabled: Boolean) {
        setUserDisabledCalls += uid to disabled
        usersFlow.value =
            usersFlow.value.map { if (it.uid == uid) it.copy(disabled = disabled) else it }
    }
}
