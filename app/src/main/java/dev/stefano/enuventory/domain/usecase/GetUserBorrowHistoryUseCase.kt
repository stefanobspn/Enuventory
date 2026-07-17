package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.repository.BorrowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** UseCase untuk mengambil riwayat peminjaman milik satu user. Dipakai di History page. */
class GetUserBorrowHistoryUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    operator fun invoke(userId: String): Flow<List<BorrowRecord>> =
        borrowRepository.getBorrowRecordsByUser(userId)
}
