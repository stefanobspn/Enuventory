package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.AuthRepository
import javax.inject.Inject

/** UseCase untuk logout dari aplikasi. */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.signOut()
}
