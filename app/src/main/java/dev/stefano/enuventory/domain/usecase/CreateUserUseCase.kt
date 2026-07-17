package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.repository.UserRepository
import javax.inject.Inject

/** UseCase untuk membuat akun RegularUser baru. Dipakai di layar Kelola User (Admin). */
class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): User =
        userRepository.createUser(name, email, password)
}
