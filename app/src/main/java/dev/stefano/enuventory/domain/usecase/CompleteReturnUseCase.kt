package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

/** UseCase admin memproses pengembalian (kondisi normal/rusak). Dipakai di Detail Request page. */
class CompleteReturnUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    suspend operator fun invoke(
        recordId: String,
        assetId: String,
        isDamaged: Boolean,
        damageNotes: String?
    ) = borrowRepository.completeReturn(recordId, assetId, isDamaged, damageNotes)
}
