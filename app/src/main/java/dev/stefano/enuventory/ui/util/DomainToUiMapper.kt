package dev.stefano.enuventory.ui.util

import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.ui.components.EnuBorrowStatus
import dev.stefano.enuventory.ui.components.EnuInventoryStatus

/**
 * Extension functions untuk memetakan domain enums ke UI enums.
 *
 * Aturan arah: domain → UI (bukan sebaliknya).
 * Mapping ini hanya boleh ada di layer UI.
 */

fun AssetStatus.toUiStatus(): EnuInventoryStatus = when (this) {
    AssetStatus.Available -> EnuInventoryStatus.Tersedia
    AssetStatus.Reserved -> EnuInventoryStatus.Direservasi
    AssetStatus.Maintenance -> EnuInventoryStatus.Maintenance
}

fun BorrowStatus.toUiStatus(): EnuBorrowStatus = when (this) {
    BorrowStatus.Pending -> EnuBorrowStatus.Menunggu
    BorrowStatus.WaitingPickup -> EnuBorrowStatus.MenungguPengambilan
    BorrowStatus.Borrowed -> EnuBorrowStatus.Dipinjam
    BorrowStatus.Rejected -> EnuBorrowStatus.Ditolak
    BorrowStatus.Completed -> EnuBorrowStatus.Selesai
    BorrowStatus.Damaged -> EnuBorrowStatus.Rusak
}

/**
 * Status efektif sebuah record untuk badge/list: "Terlambat" jika sedang
 * dipinjam dan sudah melewati tenggat (dihitung on-the-fly, tidak pernah
 * tersimpan di Firestore), selain itu mapping status biasa.
 */
fun BorrowRecord.toUiStatus(nowMillis: Long = System.currentTimeMillis()): EnuBorrowStatus =
    if (isOverdue(nowMillis)) EnuBorrowStatus.Terlambat else status.toUiStatus()
