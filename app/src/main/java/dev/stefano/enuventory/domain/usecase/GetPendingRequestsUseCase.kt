package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.repository.BorrowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** UseCase untuk mengambil semua request yang statusnya Pending. Dipakai di Approval page (Admin). */
class GetPendingRequestsUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    operator fun invoke(): Flow<List<BorrowRecord>> = borrowRepository.getPendingRequests()
}
