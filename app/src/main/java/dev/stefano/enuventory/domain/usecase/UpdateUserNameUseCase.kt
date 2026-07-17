package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.UserRepository
import javax.inject.Inject

/** UseCase untuk mengubah nama akun user. */
class UpdateUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String, name: String) =
        userRepository.updateUserName(uid, name)
}
