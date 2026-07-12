package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

/** UseCase untuk mengajukan request peminjaman. Dipakai di Detail Asset page (User). */
class RequestBorrowUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    suspend operator fun invoke(
        assetId: String,
        assetTitle: String,
        assetStock: Int,
        userId: String,
        userName: String,
        returnEstimate: String
    ) = borrowRepository.requestBorrow(assetId, assetTitle, assetStock, userId, userName, returnEstimate)
}
