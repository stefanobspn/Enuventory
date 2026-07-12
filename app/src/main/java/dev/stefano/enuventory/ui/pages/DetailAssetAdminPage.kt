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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuInventoryStatus
import dev.stefano.enuventory.ui.components.EnuInventoryStatusBadge
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme

enum class DetailAssetAdminState {
    Normal, Error
}

@Composable
fun DetailAssetAdminPage(
    state: DetailAssetAdminState,
    title: String,
    id: String,
    stock: Int,
    status: EnuInventoryStatus,
    description: String,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onHapusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                )

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
                            text = title,
                            style = EnuTheme.typography.content.headings.h3,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                        Text(
                            text = "ID: $id",
                            style = EnuTheme.typography.content.headings.h6,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                        Text(
                            text = "Stock: $stock",
                            style = EnuTheme.typography.content.headings.h6,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        EnuInventoryStatusBadge(status = status)
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
                            text = description,
                            style = EnuTheme.typography.content.body.medium,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                    }
                }

                when (state) {
                    DetailAssetAdminState.Normal -> {
                        EnuButton(
                            text = "Edit Asset",
                            onClick = onEditClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                        EnuButton(
                            text = "Hapus Asset",
                            onClick = onHapusClick,
                            modifier = Modifier.fillMaxWidth(),
                            variant = EnuButtonVariant.Danger
                        )
                    }

                    DetailAssetAdminState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            EnuButton(
                                text = "Edit Asset",
                                onClick = onEditClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                            EnuButton(
                                text = "Hapus Asset",
                                onClick = onHapusClick,
                                modifier = Modifier.fillMaxWidth(),
                                variant = EnuButtonVariant.Danger
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_error),
                                    contentDescription = null,
                                    tint = EnuTheme.colors.contentSignalErrorDefault,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Terjadi Kesalahan",
                                    style = EnuTheme.typography.ui.labels.normalCase.large,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                                Text(
                                    text = "error log",
                                    style = EnuTheme.typography.ui.labels.normalCase.small,
                                    color = EnuTheme.colors.contentSignalErrorDefault
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun DetailAssetAdminNormalPreviewLight() {
    EnuTheme {
        DetailAssetAdminPage(
            state = DetailAssetAdminState.Normal,
            title = "Title", id = "HW-0018-A", stock = 5,
            status = EnuInventoryStatus.Tersedia,
            description = "Lorem Ipsum Dolor Sit Amet",
            currentRoute = "home", onBottomBarItemClick = {},
            onBackClick = {}, onEditClick = {}, onHapusClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun DetailAssetAdminNormalPreviewDark() {
    EnuTheme(darkTheme = true) {
        DetailAssetAdminPage(
            state = DetailAssetAdminState.Normal,
            title = "Title", id = "HW-0018-A", stock = 5,
            status = EnuInventoryStatus.Tersedia,
            description = "Lorem Ipsum Dolor Sit Amet",
            currentRoute = "home", onBottomBarItemClick = {},
            onBackClick = {}, onEditClick = {}, onHapusClick = {}
        )
    }
}