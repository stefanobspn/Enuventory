package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.AuthRepository
import javax.inject.Inject

/** UseCase untuk login dengan email dan password. */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) =
        authRepository.signIn(email, password)
}
