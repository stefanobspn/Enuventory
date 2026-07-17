package dev.stefano.enuventory.ui.screen.notification

/**
 * Notifikasi in-app -- bukan push notification asli (butuh Cloud Functions/FCM yang gak ada
 * di proyek ini), cuma daftar real-time yang dihitung dari data Firestore yang udah ada
 * (request pending buat Admin, batas pengembalian mendekati buat User).
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String
)
