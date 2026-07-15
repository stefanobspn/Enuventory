package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

/** UseCase untuk admin menolak request peminjaman (atau user membatalkan) dengan alasan. */
class RejectRequestUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    suspend operator fun invoke(recordId: String, rejectionReason: String) =
        borrowRepository.rejectRequest(recordId, rejectionReason)
}
