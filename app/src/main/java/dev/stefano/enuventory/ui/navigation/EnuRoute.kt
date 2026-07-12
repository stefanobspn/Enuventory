package dev.stefano.enuventory.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Definisi semua route navigasi secara type-safe menggunakan @Serializable.
 *
 * Menggantikan raw strings seperti "home", "history", "settings" yang
 * rawan typo dan tidak membawa parameter dengan aman.
 *
 * Penggunaan:
 * ```
 * navController.navigate(EnuRoute.Home)
 * navController.navigate(EnuRoute.DetailAsset(assetId = "HW-001"))
 * ```
 */
sealed interface EnuRoute {

    // ── Auth ────────────────────────────────────────────────────────────────
    @Serializable data object Login : EnuRoute

    // ── User screens ────────────────────────────────────────────────────────
    @Serializable data object Home : EnuRoute
    @Serializable data object History : EnuRoute
    @Serializable data object Settings : EnuRoute

    @Serializable data class DetailAsset(val assetId: String) : EnuRoute
    @Serializable data class DetailRiwayat(val recordId: String) : EnuRoute

    // ── Admin screens ────────────────────────────────────────────────────────
    @Serializable data object Approval : EnuRoute
    @Serializable data object TambahAsset : EnuRoute

    @Serializable data class DetailAssetAdmin(val assetId: String) : EnuRoute
    @Serializable data class DetailRequest(val recordId: String) : EnuRoute

    // ── Shared screens ───────────────────────────────────────────────────────
    @Serializable data object ScanQR : EnuRoute
    @Serializable data class UploadBuktiFoto(val recordId: String) : EnuRoute
    @Serializable data class Pengembalian(val recordId: String) : EnuRoute
}
