package dev.stefano.enuventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.ui.screen.auth.AuthViewModel
import dev.stefano.enuventory.ui.pages.ApprovalPage
import dev.stefano.enuventory.ui.pages.DetailAssetAdminPage
import dev.stefano.enuventory.ui.pages.DetailAssetUserPage
import dev.stefano.enuventory.ui.pages.DetailRequestPage
import dev.stefano.enuventory.ui.pages.DetailRiwayatPage
import dev.stefano.enuventory.ui.pages.HistoryPage
import dev.stefano.enuventory.ui.pages.HomeAdminPage
import dev.stefano.enuventory.ui.pages.HomeUserPage
import dev.stefano.enuventory.ui.pages.PengembalianPage
import dev.stefano.enuventory.ui.pages.ScanQRPage
import dev.stefano.enuventory.ui.pages.SettingsAdminPage
import dev.stefano.enuventory.ui.pages.SettingsUserPage
import dev.stefano.enuventory.ui.pages.TambahAssetPage
import dev.stefano.enuventory.ui.pages.UploadBuktiFotoPage

/**
 * NavGraph utama aplikasi.
 *
 * - Otomatis routing ke home/login berdasarkan auth state dari AuthViewModel.
 * - isAdmin ditentukan dari currentUser.role — bukan dari parameter tersebar.
 * - Semua route type-safe via EnuRoute sealed interface.
 */
@Composable
fun EnuNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin = currentUser?.role == UserRole.Admin

    // Helper: navigasi ke bottom bar item dengan popUpTo agar back stack bersih
    val onBottomBarClick: (EnuRoute) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Start destination berdasarkan auth state
    val startDestination: EnuRoute = if (currentUser != null) EnuRoute.Home else EnuRoute.Login

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ── Auth ────────────────────────────────────────────────────────────
        composable<EnuRoute.Login> {
            // TODO: Buat LoginScreen dan LoginViewModel
            // Sementara langsung navigate ke Home untuk development
        }

        // ── Home (User / Admin) ─────────────────────────────────────────────
        composable<EnuRoute.Home> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (isAdmin) {
                HomeAdminPage(
                    state = dev.stefano.enuventory.ui.pages.HomeAdminState.Normal,
                    currentRoute = currentRoute,
                    onBottomBarItemClick = { item ->
                        val route = when (item.route) {
                            "home" -> EnuRoute.Home
                            "approval" -> EnuRoute.Approval
                            "settings" -> EnuRoute.Settings
                            else -> EnuRoute.Home
                        }
                        onBottomBarClick(route)
                    },
                    onRetryClick = {},
                    onFabClick = { navController.navigate(EnuRoute.TambahAsset) }
                )
            } else {
                HomeUserPage(
                    state = dev.stefano.enuventory.ui.pages.HomeUserState.Normal,
                    currentRoute = currentRoute,
                    onBottomBarItemClick = { item ->
                        val route = when (item.route) {
                            "home" -> EnuRoute.Home
                            "history" -> EnuRoute.History
                            "settings" -> EnuRoute.Settings
                            else -> EnuRoute.Home
                        }
                        onBottomBarClick(route)
                    },
                    onRetryClick = {},
                    isAdmin = false
                )
            }
        }

        // ── History (User) ──────────────────────────────────────────────────
        composable<EnuRoute.History> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            HistoryPage(
                state = dev.stefano.enuventory.ui.pages.HistoryPageState.Normal,
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val route = when (item.route) {
                        "home" -> EnuRoute.Home
                        "history" -> EnuRoute.History
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(route)
                },
                onRetryClick = {},
                onDetailClick = { recordId ->
                    navController.navigate(EnuRoute.DetailRiwayat(recordId))
                },
                isAdmin = false
            )
        }

        // ── Settings (User / Admin) ─────────────────────────────────────────
        composable<EnuRoute.Settings> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (isAdmin) {
                SettingsAdminPage(
                    username = currentUser?.name ?: "",
                    role = "Admin",
                    appVersion = "v1.0.0",
                    currentRoute = currentRoute,
                    onBottomBarItemClick = { item ->
                        val route = when (item.route) {
                            "home" -> EnuRoute.Home
                            "approval" -> EnuRoute.Approval
                            "settings" -> EnuRoute.Settings
                            else -> EnuRoute.Home
                        }
                        onBottomBarClick(route)
                    },
                    onSignOutClick = { authViewModel.signOut() },
                    isAdmin = true
                )
            } else {
                SettingsUserPage(
                    username = currentUser?.name ?: "",
                    role = "User",
                    appVersion = "v1.0.0",
                    currentRoute = currentRoute,
                    onBottomBarItemClick = { item ->
                        val route = when (item.route) {
                            "home" -> EnuRoute.Home
                            "history" -> EnuRoute.History
                            "settings" -> EnuRoute.Settings
                            else -> EnuRoute.Home
                        }
                        onBottomBarClick(route)
                    },
                    onSignOutClick = { authViewModel.signOut() },
                    isAdmin = false
                )
            }
        }

        // ── Approval (Admin) ────────────────────────────────────────────────
        composable<EnuRoute.Approval> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            ApprovalPage(
                state = dev.stefano.enuventory.ui.pages.ApprovalPageState.Normal,
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val route = when (item.route) {
                        "home" -> EnuRoute.Home
                        "approval" -> EnuRoute.Approval
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(route)
                },
                onRetryClick = {},
                onDetailClick = { recordId ->
                    navController.navigate(EnuRoute.DetailRequest(recordId))
                },
                isAdmin = true
            )
        }

        // ── Tambah Asset (Admin) ────────────────────────────────────────────
        composable<EnuRoute.TambahAsset> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            TambahAssetPage(
                state = dev.stefano.enuventory.ui.pages.TambahAssetState.Normal,
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val route = when (item.route) {
                        "home" -> EnuRoute.Home
                        "approval" -> EnuRoute.Approval
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(route)
                },
                onBackClick = { navController.popBackStack() },
                onAddPhotoClick = {},
                onTambahAssetClick = { _, _, _, _, _ -> },
                onRetryClick = {}
            )
        }

        // ── Detail Asset (User) ─────────────────────────────────────────────
        composable<EnuRoute.DetailAsset> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailAsset>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            // TODO: Ganti dengan data nyata dari DetailAssetViewModel nanti
            DetailAssetUserPage(
                state = dev.stefano.enuventory.ui.pages.DetailAssetUserState.Normal,
                title = "",
                id = route.assetId,
                stock = 0,
                status = dev.stefano.enuventory.ui.components.EnuInventoryStatus.Tersedia,
                description = "",
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val navRoute = when (item.route) {
                        "home" -> EnuRoute.Home
                        "history" -> EnuRoute.History
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(navRoute)
                },
                onBackClick = { navController.popBackStack() },
                onPinjamClick = {},
                onBatalkanClick = {}
            )
        }

        // ── Detail Asset (Admin) ────────────────────────────────────────────
        composable<EnuRoute.DetailAssetAdmin> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailAssetAdmin>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            DetailAssetAdminPage(
                state = dev.stefano.enuventory.ui.pages.DetailAssetAdminState.Normal,
                title = "", id = route.assetId, stock = 0,
                status = dev.stefano.enuventory.ui.components.EnuInventoryStatus.Tersedia,
                description = "",
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val navRoute = when (item.route) {
                        "home" -> EnuRoute.Home
                        "approval" -> EnuRoute.Approval
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(navRoute)
                },
                onBackClick = { navController.popBackStack() },
                onEditClick = {},
                onHapusClick = {}
            )
        }

        // ── Detail Riwayat (User) ───────────────────────────────────────────
        composable<EnuRoute.DetailRiwayat> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailRiwayat>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            DetailRiwayatPage(
                state = dev.stefano.enuventory.ui.pages.DetailRiwayatState.MenungguPersetujuan,
                assetTitle = "",
                assetId = route.recordId,
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val navRoute = when (item.route) {
                        "home" -> EnuRoute.Home
                        "history" -> EnuRoute.History
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(navRoute)
                },
                onBackClick = { navController.popBackStack() },
                onScanQrClick = { navController.navigate(EnuRoute.ScanQR) },
                onKembalikanClick = {
                    navController.navigate(EnuRoute.Pengembalian(route.recordId))
                }
            )
        }

        // ── Detail Request (Admin) ──────────────────────────────────────────
        composable<EnuRoute.DetailRequest> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailRequest>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            // TODO: Ganti dengan data nyata dari DetailRequestViewModel nanti
            DetailRequestPage(
                state = dev.stefano.enuventory.ui.pages.DetailRequestState.Normal,
                assetTitle = "",
                assetId = "",
                borrowDate = "",
                returnEstimate = "",
                message = "",
                currentRoute = currentRoute,
                onBottomBarItemClick = { item ->
                    val navRoute = when (item.route) {
                        "home" -> EnuRoute.Home
                        "approval" -> EnuRoute.Approval
                        "settings" -> EnuRoute.Settings
                        else -> EnuRoute.Home
                    }
                    onBottomBarClick(navRoute)
                },
                onBackClick = { navController.popBackStack() },
                onApproveClick = {},
                onTolakClick = {},
                onRetryClick = {}
            )
        }

        // ── Scan QR ─────────────────────────────────────────────────────────
        composable<EnuRoute.ScanQR> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            ScanQRPage(
                state = dev.stefano.enuventory.ui.pages.ScanQRState.Scanning,
                currentRoute = currentRoute,
                onBottomBarItemClick = {},
                onBackClick = { navController.popBackStack() },
                onUlangiClick = {},
                onKonfirmasiYaClick = { navController.popBackStack() }
            )
        }

        // ── Upload Bukti Foto ────────────────────────────────────────────────
        composable<EnuRoute.UploadBuktiFoto> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.UploadBuktiFoto>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            UploadBuktiFotoPage(
                state = dev.stefano.enuventory.ui.pages.UploadBuktiState.Capture,
                assetTitle = "",
                assetId = "",
                timestamp = "",
                currentRoute = currentRoute,
                onBottomBarItemClick = {},
                onBackClick = { navController.popBackStack() },
                onCaptureClick = {},
                onUlangiClick = {},
                onSubmitClick = {}
            )
        }

        // ── Pengembalian ─────────────────────────────────────────────────────
        composable<EnuRoute.Pengembalian> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.Pengembalian>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            PengembalianPage(
                assetTitle = "",
                assetId = route.recordId,
                currentRoute = currentRoute,
                onBottomBarItemClick = {},
                onBackClick = { navController.popBackStack() },
                onUploadBuktiClick = {
                    navController.navigate(EnuRoute.UploadBuktiFoto(route.recordId))
                }
            )
        }
    }
}
