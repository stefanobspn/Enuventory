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
import dev.stefano.enuventory.domain.model.AppThemeMode
import dev.stefano.enuventory.ui.screen.auth.AuthViewModel
import dev.stefano.enuventory.ui.screen.auth.LoginScreen
import dev.stefano.enuventory.ui.screen.home.HomeViewModel
import dev.stefano.enuventory.ui.screen.history.HistoryViewModel
import dev.stefano.enuventory.ui.screen.history.DetailRiwayatViewModel
import dev.stefano.enuventory.ui.screen.history.ReturnAssetViewModel
import dev.stefano.enuventory.ui.common.UiState
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import dev.stefano.enuventory.ui.screen.approval.ApprovalViewModel
import dev.stefano.enuventory.ui.screen.approval.DetailRequestViewModel
import dev.stefano.enuventory.ui.screen.settings.SettingsViewModel
import dev.stefano.enuventory.ui.screen.asset.TambahAssetViewModel
import dev.stefano.enuventory.ui.screen.asset.DetailAssetUserViewModel
import dev.stefano.enuventory.ui.screen.asset.DetailAssetAdminViewModel
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
import dev.stefano.enuventory.ui.theme.EnuTheme

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
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(EnuRoute.Home) {
                        popUpTo(EnuRoute.Login) { inclusive = true }
                    }
                }
            )
        }

        // ── Home (User / Admin) ─────────────────────────────────────────────
        composable<EnuRoute.Home> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val homeViewModel: HomeViewModel = hiltViewModel()
            val assetsState by homeViewModel.assetsState.collectAsStateWithLifecycle()

            if (isAdmin) {
                HomeAdminPage(
                    state = assetsState,
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
                    onFabClick = { navController.navigate(EnuRoute.TambahAsset) },
                    onAssetClick = { assetId -> navController.navigate(EnuRoute.DetailAssetAdmin(assetId)) }
                )
            } else {
                HomeUserPage(
                    state = assetsState,
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
                    onAssetClick = { assetId -> navController.navigate(EnuRoute.DetailAsset(assetId)) },
                    isAdmin = false
                )
            }
        }

        // ── History (User) ──────────────────────────────────────────────────
        composable<EnuRoute.History> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val historyViewModel: HistoryViewModel = hiltViewModel()
            val historyState by historyViewModel.historyState.collectAsStateWithLifecycle()

            HistoryPage(
                state = historyState,
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
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

            if (isAdmin) {
                SettingsAdminPage(
                    username = currentUser?.name ?: "",
                    role = "Admin",
                    appVersion = "v1.0.0",
                    currentRoute = currentRoute,
                    selectedTheme = themeMode,
                    onThemeSelected = { settingsViewModel.setThemeMode(it) },
                    onBottomBarItemClick = { item ->
                        val route = when (item.route) {
                            "home" -> EnuRoute.Home
                            "approval" -> EnuRoute.Approval
                            "settings" -> EnuRoute.Settings
                            else -> EnuRoute.Home
                        }
                        onBottomBarClick(route)
                    },
                    onSignOutClick = { settingsViewModel.signOut() },
                    isAdmin = true
                )
            } else {
                SettingsUserPage(
                    username = currentUser?.name ?: "",
                    role = "User",
                    appVersion = "v1.0.0",
                    currentRoute = currentRoute,
                    selectedTheme = themeMode,
                    onThemeSelected = { settingsViewModel.setThemeMode(it) },
                    onBottomBarItemClick = { item ->
                        val route = when (item.route) {
                            "home" -> EnuRoute.Home
                            "history" -> EnuRoute.History
                            "settings" -> EnuRoute.Settings
                            else -> EnuRoute.Home
                        }
                        onBottomBarClick(route)
                    },
                    onSignOutClick = { settingsViewModel.signOut() },
                    isAdmin = false
                )
            }
        }

        // ── Approval (Admin) ────────────────────────────────────────────────
        composable<EnuRoute.Approval> {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val approvalViewModel: ApprovalViewModel = hiltViewModel()
            val requestsState by approvalViewModel.requestsState.collectAsStateWithLifecycle()

            ApprovalPage(
                state = requestsState,
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
            val tambahAssetViewModel: TambahAssetViewModel = hiltViewModel()
            val addState by tambahAssetViewModel.addState.collectAsStateWithLifecycle()

            TambahAssetPage(
                state = addState,
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
                onTambahAssetClick = { title, stock, status, category, description ->
                    tambahAssetViewModel.addAsset(
                        title = title,
                        stockStr = stock,
                        statusStr = status,
                        category = category,
                        description = description,
                        onSuccess = { navController.popBackStack() }
                    )
                },
                onRetryClick = { tambahAssetViewModel.resetState() }
            )
        }

        // ── Detail Asset (User) ─────────────────────────────────────────────
        composable<EnuRoute.DetailAsset> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailAsset>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val detailAssetUserViewModel: DetailAssetUserViewModel = hiltViewModel()
            val uiState by detailAssetUserViewModel.uiState.collectAsStateWithLifecycle()

            DetailAssetUserPage(
                state = uiState,
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
                onPinjamClick = { returnEstimate ->
                    detailAssetUserViewModel.requestBorrow(returnEstimate)
                },
                onBatalkanClick = { recordId ->
                    detailAssetUserViewModel.cancelBorrow(recordId)
                },
                onRetryClick = {}
            )
        }

        // ── Detail Asset (Admin) ────────────────────────────────────────────
        composable<EnuRoute.DetailAssetAdmin> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailAssetAdmin>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val detailAssetAdminViewModel: DetailAssetAdminViewModel = hiltViewModel()
            val assetState by detailAssetAdminViewModel.assetState.collectAsStateWithLifecycle()

            DetailAssetAdminPage(
                state = assetState,
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
                onHapusClick = {
                    detailAssetAdminViewModel.deleteAsset(onSuccess = { navController.popBackStack() })
                },
                onRetryClick = {}
            )
        }

        // ── Detail Riwayat (User) ───────────────────────────────────────────
        composable<EnuRoute.DetailRiwayat> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailRiwayat>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val detailRiwayatViewModel: DetailRiwayatViewModel = hiltViewModel()
            val uiState by detailRiwayatViewModel.uiState.collectAsStateWithLifecycle()

            DetailRiwayatPage(
                state = uiState,
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
                },
                onRetryClick = { detailRiwayatViewModel.loadRecord() }
            )
        }

        // ── Detail Request (Admin) ──────────────────────────────────────────
        composable<EnuRoute.DetailRequest> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.DetailRequest>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val detailRequestViewModel: DetailRequestViewModel = hiltViewModel()
            val uiState by detailRequestViewModel.uiState.collectAsStateWithLifecycle()

            DetailRequestPage(
                state = uiState,
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
                onApproveClick = {
                    detailRequestViewModel.approveRequest(onSuccess = { navController.popBackStack() })
                },
                onTolakClick = {
                    detailRequestViewModel.rejectRequest(onSuccess = { navController.popBackStack() })
                },
                onRetryClick = { detailRequestViewModel.loadRecord() }
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
            val returnAssetViewModel: ReturnAssetViewModel = hiltViewModel()
            val recordState by returnAssetViewModel.recordState.collectAsStateWithLifecycle()
            val uploadState by returnAssetViewModel.uploadState.collectAsStateWithLifecycle()

            when (val state = recordState) {
                is UiState.Success -> {
                    val record = state.data
                    UploadBuktiFotoPage(
                        state = uploadState,
                        assetTitle = record.assetTitle,
                        assetId = record.assetId,
                        timestamp = record.borrowDate,
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
                        onCaptureClick = { returnAssetViewModel.onCapturePhoto() },
                        onUlangiClick = { returnAssetViewModel.onUlangiCapture() },
                        onSubmitClick = {
                            returnAssetViewModel.submitReturn(onSuccess = {
                                navController.navigate(EnuRoute.History) {
                                    popUpTo(EnuRoute.History) { inclusive = true }
                                }
                            })
                        }
                    )
                }
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EnuTheme.colors.contentBrandPrimaryDefault)
                    }
                }
                else -> {
                    // Fallback
                }
            }
        }

        // ── Pengembalian ─────────────────────────────────────────────────────
        composable<EnuRoute.Pengembalian> { backStackEntry ->
            val route = backStackEntry.toRoute<EnuRoute.Pengembalian>()
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val returnAssetViewModel: ReturnAssetViewModel = hiltViewModel()
            val recordState by returnAssetViewModel.recordState.collectAsStateWithLifecycle()

            when (val state = recordState) {
                is UiState.Success -> {
                    val record = state.data
                    PengembalianPage(
                        assetTitle = record.assetTitle,
                        assetId = record.assetId,
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
                        onUploadBuktiClick = {
                            navController.navigate(EnuRoute.UploadBuktiFoto(route.recordId))
                        }
                    )
                }
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EnuTheme.colors.contentBrandPrimaryDefault)
                    }
                }
                else -> {
                    // Fallback
                }
            }
        }
    }
}
