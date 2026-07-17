package dev.stefano.enuventory.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pemformatan tanggal untuk layer UI.
 *
 * Domain menyimpan semua tanggal sebagai epoch millis (Long) — teks hanya
 * dibentuk di sini, tepat sebelum dirender.
 */

private const val DATE_PATTERN = "dd MMM yyyy"
private const val DATE_TIME_PATTERN = "dd MMM yyyy, HH:mm"

/** Format "dd MMM yyyy" — untuk tanggal pinjam/kembali. */
fun formatDate(millis: Long): String =
    SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date(millis))

/** Format "dd MMM yyyy, HH:mm" — untuk waktu request/jadwal pengambilan. */
fun formatDateTime(millis: Long): String =
    SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault()).format(Date(millis))
