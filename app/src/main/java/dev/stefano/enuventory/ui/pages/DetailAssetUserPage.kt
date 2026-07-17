package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBorrowDialog
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuInventoryStatusBadge
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.screen.asset.DetailAssetUiModel
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.toUiStatus

enum class DetailAssetUserState {
    Normal, Error, MenungguPersetujuan, MenungguPengambilan, SedangDipinjam, TidakTersedia
}

@Composable
fun DetailAssetUserPage(
    state: UiState<DetailAssetUiModel>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onPinjamClick: (borrowDateMillis: Long, returnEstimateMillis: Long, alasan: String) -> Unit,
    onBatalkanClick: (recordId: String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBorrowDialog by remember { mutableStateOf(false) }
    var isDialogSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            val relationshipState = state.data.relationshipState
            if (relationshipState != DetailAssetUserState.Normal) {
                showBorrowDialog = false
                isDialogSubmitting = false
            }
        }
    }

    if (showBorrowDialog) {
        EnuBorrowDialog(
            onDismissRequest = { showBorrowDialog = false },
            isSubmitting = isDialogSubmitting,
            onSubmitClick = { borrowMillis, returnMillis, alasan ->
                isDialogSubmitting = true
                onPinjamClick(borrowMillis, returnMillis, alasan)
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Detail Asset",
                showBack = true,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            EnuBottomBar(
                isAdmin = false,
                currentRoute = currentRoute,
                onItemClick = onBottomBarItemClick
            )
        },
        containerColor = EnuTheme.colors.surfaceDefaultBase
    ) { innerPadding ->
        when (state) {
            is UiState.Success -> {
                val data = state.data
                val asset = data.asset
                val relationshipState = data.relationshipState
                val activeRecordId = data.activeRecordId

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color(0xFFB0B0B0))
                        ) {
                            if (asset.imageUrl != null) {
                                AsyncImage(
                                    model = asset.imageUrl,
                                    contentDescription = asset.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .align(Alignment.BottomCenter),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = asset.title,
                                    style = EnuTheme.typography.content.headings.h3,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                                Text(
                                    text = "ID: ${asset.id}",
                                    style = EnuTheme.typography.content.headings.h6,
                                    color = EnuTheme.colors.contentDefaultSubtle
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                EnuInventoryStatusBadge(status = asset.status.toUiStatus())
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
                            border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Deskripsi",
                                    style = EnuTheme.typography.content.headings.h3,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                                Text(
                                    text = asset.description.ifBlank { "Tidak ada deskripsi." },
                                    style = EnuTheme.typography.content.body.medium,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                        }

                        when (relationshipState) {
                            DetailAssetUserState.Normal -> {
                                EnuButton(
                                    text = "Pinjam Asset",
                                    onClick = { showBorrowDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            DetailAssetUserState.TidakTersedia -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Asset sedang tidak tersedia",
                                        style = EnuTheme.typography.ui.labels.normalCase.large,
                                        color = EnuTheme.colors.contentDefaultSubtle,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            DetailAssetUserState.MenungguPengambilan -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Menunggu Pengambilan",
                                        style = EnuTheme.typography.ui.labels.normalCase.large,
                                        color = EnuTheme.colors.contentSignalWarningDefault,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            DetailAssetUserState.MenungguPersetujuan -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Menunggu Persetujuan",
                                            style = EnuTheme.typography.ui.labels.normalCase.large,
                                            color = EnuTheme.colors.contentSignalWarningDefault,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    EnuButton(
                                        text = "Batalkan",
                                        variant = EnuButtonVariant.Danger,
                                        onClick = {
                                            activeRecordId?.let { onBatalkanClick(it) }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            DetailAssetUserState.SedangDipinjam -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sedang Dipinjam",
                                        style = EnuTheme.typography.ui.labels.normalCase.large,
                                        color = EnuTheme.colors.contentSignalSuccessDefault,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            DetailAssetUserState.Error -> {
                                // Fallback jika tidak terdefinisi
                            }
                        }
                    }
                }
            }

            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = EnuTheme.colors.contentBrandPrimaryDefault
                    )
                }
            }

            is UiState.Error -> {
                EnuErrorState(errorMessage = state.message, onRetryClick = onRetryClick)
            }

            is UiState.Empty -> {
                EnuEmptyState("Asset tidak ditemukan")
            }
        }
    }
}

@Preview(name = "Normal")
@Composable
fun DetailAssetNormalPreview() {
    val dummyAsset = Asset(
        id = "HW-001",
        title = "Macbook Pro 14",
        status = AssetStatus.Available,
        category = "Elektro",
        description = "Laptop untuk programming"
    )
    EnuTheme {
        DetailAssetUserPage(
            state = UiState.Success(
                DetailAssetUiModel(dummyAsset, DetailAssetUserState.Normal)
            ),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onPinjamClick = { _, _, _ -> },
            onBatalkanClick = {},
            onRetryClick = {}
        )
    }
}