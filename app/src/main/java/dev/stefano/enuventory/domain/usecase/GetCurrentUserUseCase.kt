package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** UseCase untuk observe user yang sedang login. Mengembalikan null jika belum login. */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> = authRepository.getCurrentUser()
}
