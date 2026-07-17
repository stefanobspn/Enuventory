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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuConfirmationDialog
import dev.stefano.enuventory.ui.components.EnuInventoryStatusBadge
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.toUiStatus

@Composable
fun DetailAssetAdminPage(
    state: UiState<Asset>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onHapusClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        EnuConfirmationDialog(
            title = "Hapus Asset",
            message = "Asset yang sudah dihapus tidak bisa dikembalikan. Lanjutkan?",
            onConfirmClick = {
                showDeleteConfirmation = false
                onHapusClick()
            },
            onDismissRequest = { showDeleteConfirmation = false }
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
                isAdmin = true,
                currentRoute = currentRoute,
                onItemClick = onBottomBarItemClick
            )
        },
        containerColor = EnuTheme.colors.surfaceDefaultBase
    ) { innerPadding ->
        when (state) {
            is UiState.Success -> {
                val asset = state.data
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

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            EnuButton(
                                text = "Edit Asset",
                                onClick = onEditClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                            EnuButton(
                                text = "Hapus Asset",
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                                variant = EnuButtonVariant.Danger
                            )
                        }
                    }
                }
            }

            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
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

@Preview(showBackground = true, name = "Light")
@Composable
fun DetailAssetAdminNormalPreviewLight() {
    val dummyAsset = Asset(
        id = "HW-001",
        title = "Macbook Pro 14",
        status = AssetStatus.Available,
        category = "Elektro",
        description = "Laptop untuk programming"
    )
    EnuTheme {
        DetailAssetAdminPage(
            state = UiState.Success(dummyAsset),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onEditClick = {},
            onHapusClick = {},
            onRetryClick = {}
        )
    }
}