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
    @Serializable
    data object Notifikasi : EnuRoute

    @Serializable data class DetailAsset(val assetId: String) : EnuRoute
    @Serializable data class DetailRiwayat(val recordId: String) : EnuRoute

    // ── Admin screens ────────────────────────────────────────────────────────
    @Serializable data object Approval : EnuRoute
    @Serializable data object TambahAsset : EnuRoute

    @Serializable data class DetailAssetAdmin(val assetId: String) : EnuRoute
    @Serializable
    data class EditAsset(val assetId: String) : EnuRoute
    @Serializable
    data object KelolaKategori : EnuRoute
    @Serializable
    data object KelolaUser : EnuRoute
    @Serializable
    data class DetailUserAdmin(val userId: String) : EnuRoute
    @Serializable data class DetailRequest(val recordId: String) : EnuRoute

    // ── Shared screens ───────────────────────────────────────────────────────
    @Serializable
    data class ScanQR(val recordId: String, val assetId: String) : EnuRoute
}
