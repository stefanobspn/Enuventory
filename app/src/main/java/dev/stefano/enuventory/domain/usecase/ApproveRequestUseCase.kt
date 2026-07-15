package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

/** UseCase untuk admin menyetujui request peminjaman + menetapkan jadwal pengambilan. */
class ApproveRequestUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    suspend operator fun invoke(recordId: String, assetId: String, pickupSchedule: Long) =
        borrowRepository.approveRequest(recordId, assetId, pickupSchedule)
}
