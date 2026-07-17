package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** UseCase untuk mengambil daftar semua akun RegularUser. Dipakai di layar Kelola User (Admin). */
class GetRegularUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> = userRepository.getRegularUsers()
}
