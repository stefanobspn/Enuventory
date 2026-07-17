package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.UserRepository
import javax.inject.Inject

/** UseCase untuk menonaktifkan/mengaktifkan kembali akun user. */
class SetUserDisabledUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String, disabled: Boolean) =
        userRepository.setUserDisabled(uid, disabled)
}
