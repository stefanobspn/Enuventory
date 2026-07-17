package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

class GetBorrowRecordByIdUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    suspend operator fun invoke(recordId: String): BorrowRecord? =
        borrowRepository.getBorrowRecordById(recordId)
}
