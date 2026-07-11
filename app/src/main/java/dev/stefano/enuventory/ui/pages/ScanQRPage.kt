package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme

enum class ScanQRState {
    Scanning, Error
}

@Composable
fun ScanQRPage(
    state: ScanQRState,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onUlangiClick: () -> Unit,
    onKonfirmasiYaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Konfirmasi Peminjaman",
                        style = EnuTheme.typography.ui.labels.normalCase.large,
                        color = EnuTheme.colors.contentDefaultPrimary,
                        textAlign = TextAlign.Center
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Arduino Micro Controller",
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ID : HW-0019-A",
                            style = EnuTheme.typography.ui.labels.normalCase.small,
                            color = EnuTheme.colors.contentDefaultSubtle,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Asset Code : 01A",
                            style = EnuTheme.typography.ui.labels.normalCase.small,
                            color = EnuTheme.colors.contentDefaultSubtle,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EnuTheme.colors.backgroundNeutralMediumDefault)
                                .clickable { showSuccessDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Batal",
                                style = EnuTheme.typography.ui.labels.normalCase.large,
                                color = EnuTheme.colors.contentDefaultPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EnuTheme.colors.backgroundBrandPrimaryStrongDefault)
                                .clickable {
                                    showSuccessDialog = false
                                    onKonfirmasiYaClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ya",
                                style = EnuTheme.typography.ui.labels.normalCase.large,
                                color = EnuTheme.colors.contentBrandPrimaryOnStrong
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Scan QR",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .clickable {
                        if (state == ScanQRState.Scanning) {
                            showSuccessDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state == ScanQRState.Scanning) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(12.dp))
                    )
                }

                if (state == ScanQRState.Error) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = null,
                            tint = EnuTheme.colors.contentSignalErrorDefault,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "error log",
                            style = EnuTheme.typography.ui.labels.normalCase.small,
                            color = EnuTheme.colors.contentSignalErrorDefault
                        )
                    }
                }
            }

            if (state == ScanQRState.Error) {
                EnuButton(
                    text = "Ulangi",
                    onClick = onUlangiClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun ScanQRPagePreviewLight() {
    EnuTheme {
        ScanQRPage(
            state = ScanQRState.Scanning,
            currentRoute = "history",
            onBottomBarItemClick = {},
            onBackClick = {},
            onUlangiClick = {},
            onKonfirmasiYaClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun ScanQRPagePreviewDark() {
    EnuTheme(darkTheme = true) {
        ScanQRPage(
            state = ScanQRState.Error,
            currentRoute = "history",
            onBottomBarItemClick = {},
            onBackClick = {},
            onUlangiClick = {},
            onKonfirmasiYaClick = {}
        )
    }
}