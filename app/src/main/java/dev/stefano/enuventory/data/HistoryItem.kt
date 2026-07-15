package dev.stefano.enuventory.data

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus

// TODO: File ini bersifat sementara untuk dummy data selama belum ada repository.
// Akan dihapus saat BorrowRepository sudah diimplementasikan.

// 16 Okt 2026 & sekitarnya, dalam epoch millis — cukup representatif untuk preview.
private const val DAY_MILLIS = 24L * 60 * 60 * 1000
private const val BASE_DATE_MILLIS = 1_792_195_200_000L // 16 Okt 2026 00:00 UTC

val dummyBorrowRecords = listOf(
    // Data untuk Tab Aktif
    BorrowRecord(
        id = "REQ-001",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "USR-001",
        borrowerName = "Budi Santoso",
        status = BorrowStatus.Pending,
        requestedAt = BASE_DATE_MILLIS,
        borrowDate = BASE_DATE_MILLIS,
        returnEstimate = BASE_DATE_MILLIS + 7 * DAY_MILLIS,
        reason = "Untuk praktikum IoT"
    ),
    BorrowRecord(
        id = "REQ-002",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "USR-001",
        borrowerName = "Budi Santoso",
        status = BorrowStatus.Borrowed,
        requestedAt = BASE_DATE_MILLIS,
        borrowDate = BASE_DATE_MILLIS,
        returnEstimate = BASE_DATE_MILLIS + 7 * DAY_MILLIS,
        reason = "Untuk praktikum IoT",
        pickupSchedule = BASE_DATE_MILLIS + DAY_MILLIS,
        pickedUpAt = BASE_DATE_MILLIS + DAY_MILLIS
    ),

    // Data untuk Tab Selesai
    BorrowRecord(
        id = "REQ-003",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "USR-002",
        borrowerName = "Ani Rahayu",
        status = BorrowStatus.Rejected,
        requestedAt = BASE_DATE_MILLIS,
        borrowDate = BASE_DATE_MILLIS,
        returnEstimate = BASE_DATE_MILLIS + 7 * DAY_MILLIS,
        reason = "Untuk tugas akhir",
        rejectionReason = "Barang sedang dibutuhkan kelas lain"
    ),
    BorrowRecord(
        id = "REQ-004",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "USR-002",
        borrowerName = "Ani Rahayu",
        status = BorrowStatus.Completed,
        requestedAt = BASE_DATE_MILLIS,
        borrowDate = BASE_DATE_MILLIS,
        returnEstimate = BASE_DATE_MILLIS + 7 * DAY_MILLIS,
        reason = "Untuk tugas akhir",
        pickupSchedule = BASE_DATE_MILLIS + DAY_MILLIS,
        pickedUpAt = BASE_DATE_MILLIS + DAY_MILLIS,
        returnDate = BASE_DATE_MILLIS + 5 * DAY_MILLIS
    )
)
