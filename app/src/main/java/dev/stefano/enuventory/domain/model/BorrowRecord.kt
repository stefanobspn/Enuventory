package dev.stefano.enuventory.domain.model

/**
 * Domain entity untuk sebuah record peminjaman.
 *
 * Ini adalah representasi "murni" dari data bisnis — tidak ada
 * dependency ke Android framework, Room entity, maupun Firestore DTO.
 *
 * Semua tanggal disimpan sebagai epoch millis (Long); pemformatan ke teks
 * hanya dilakukan di layer UI.
 */
data class BorrowRecord(
    val id: String,
    val assetId: String,
    val assetTitle: String,
    val borrowerId: String,
    val borrowerName: String,
    val status: BorrowStatus,
    /** Kapan request diajukan. */
    val requestedAt: Long,
    /** Tanggal pinjam pilihan user. */
    val borrowDate: Long,
    /** Tanggal kembali (tenggat) pilihan user. */
    val returnEstimate: Long,
    /** Alasan peminjaman dari user. */
    val reason: String,
    /** Jadwal pengambilan barang, diisi admin saat approve. */
    val pickupSchedule: Long? = null,
    /** Kapan QR discan / barang benar-benar diambil. */
    val pickedUpAt: Long? = null,
    /** Kapan pengembalian diproses admin. */
    val returnDate: Long? = null,
    /** Alasan penolakan dari admin. */
    val rejectionReason: String? = null,
    /** Catatan kerusakan dari admin (hanya untuk status Damaged). */
    val damageNotes: String? = null
) {
    /** Helper property — true jika request sudah selesai (ditolak/dikembalikan). */
    val isFinished: Boolean
        get() = status == BorrowStatus.Rejected ||
                status == BorrowStatus.Completed ||
                status == BorrowStatus.Damaged

    /**
     * Terlambat = sedang dipinjam dan sudah melewati tenggat kembali.
     * Dihitung on-the-fly, tidak pernah ditulis ke Firestore.
     */
    fun isOverdue(nowMillis: Long): Boolean =
        status == BorrowStatus.Borrowed && nowMillis > returnEstimate
}
