package dev.stefano.enuventory.domain.model

/**
 * Status sebuah peminjaman/request.
 * Ini adalah domain enum — tidak bergantung pada layer UI manapun.
 *
 * Catatan: "Terlambat/Overdue" BUKAN status tersimpan — dihitung client-side
 * dari [BorrowRecord.isOverdue] (returnEstimate vs waktu sekarang).
 */
enum class BorrowStatus {
    /** Menunggu persetujuan admin. */
    Pending,

    /** Disetujui — menunggu user mengambil barang sesuai jadwal. */
    WaitingPickup,

    /** Barang sudah diambil (QR discan), sedang dipinjam. */
    Borrowed,

    /** Ditolak admin / dibatalkan user. */
    Rejected,

    /** Selesai — dikembalikan dalam kondisi normal. */
    Completed,

    /** Selesai — dikembalikan dalam kondisi rusak. */
    Damaged;

    companion object {
        /** Parse string dari Firestore secara aman — nilai lama tetap valid. */
        fun fromRaw(raw: String?): BorrowStatus =
            entries.firstOrNull { it.name == raw } ?: Pending
    }
}
