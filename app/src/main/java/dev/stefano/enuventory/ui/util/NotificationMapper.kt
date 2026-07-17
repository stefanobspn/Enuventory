package dev.stefano.enuventory.ui.util

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus

private const val DEFAULT_DEADLINE_THRESHOLD_HOURS = 24L

/**
 * Filter [BorrowRecord] yang statusnya masih Borrowed dan batas kembalinya sudah lewat atau
 * tinggal [thresholdHours] jam lagi — dipakai buat notifikasi in-app "batas pengembalian
 * segera" di Home (User). Tanggal dibandingkan langsung sebagai epoch millis.
 */
fun List<BorrowRecord>.nearDeadlineRecords(
    thresholdHours: Long = DEFAULT_DEADLINE_THRESHOLD_HOURS,
    nowMillis: Long = System.currentTimeMillis()
): List<BorrowRecord> {
    val thresholdMillis = thresholdHours * 60 * 60 * 1000
    return filter { record ->
        record.status == BorrowStatus.Borrowed &&
                record.returnEstimate - nowMillis <= thresholdMillis
    }
}

/** Pesan singkat buat notifikasi, membedakan yang udah lewat batas vs yang masih mendekati. */
fun BorrowRecord.deadlineMessage(nowMillis: Long = System.currentTimeMillis()): String {
    val deadlineText = formatDate(returnEstimate)
    return if (isOverdue(nowMillis)) {
        "Sudah lewat batas kembali ($deadlineText)"
    } else {
        "Batas kembali: $deadlineText"
    }
}
