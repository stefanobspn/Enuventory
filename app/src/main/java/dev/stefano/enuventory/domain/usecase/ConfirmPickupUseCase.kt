package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.BorrowRepository
import javax.inject.Inject

/** UseCase konfirmasi pengambilan barang setelah scan QR cocok. Dipakai di Scan QR page. */
class ConfirmPickupUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository
) {
    suspend operator fun invoke(recordId: String) = borrowRepository.confirmPickup(recordId)
}
