package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

/** UseCase untuk mengambil seluruh record peminjaman. Dipakai di Approval page (Admin). */
class GetAllBorrowRecordsUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    operator fun invoke() = borrowRepository.getAllBorrowRecords()
}
