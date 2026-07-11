package dev.stefano.enuventory.data

import dev.stefano.enuventory.ui.components.EnuBorrowStatus

data class HistoryItem(
    val id: String,
    val title: String,
    val stock: Int,
    val status: EnuBorrowStatus,
    val borrowDate: String,
    val returnEstimate: String,
    val isFinished: Boolean
)

val dummyHistoryItems = listOf(
    // Data untuk Tab Aktif
    HistoryItem(
        "HW-0019-A",
        "Arduino Micro Controller",
        5,
        EnuBorrowStatus.Menunggu,
        "16 Okt 26",
        "16 Okt 26",
        isFinished = false
    ),
    HistoryItem(
        "HW-0019-A",
        "Arduino Micro Controller",
        5,
        EnuBorrowStatus.Dipinjam,
        "16 Okt 26",
        "16 Okt 26",
        isFinished = false
    ),

    // Data untuk Tab Selesai
    HistoryItem(
        "HW-0019-A",
        "Arduino Micro Controller",
        5,
        EnuBorrowStatus.Ditolak,
        "16 Okt 26",
        "-",
        isFinished = true
    ),
    HistoryItem(
        "HW-0019-A",
        "Arduino Micro Controller",
        5,
        EnuBorrowStatus.Selesai,
        "16 Okt 26",
        "21 Okt 26",
        isFinished = true
    )
)