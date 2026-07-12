package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuDetailHistoryCard
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme

enum class DetailRequestState {
    Normal, Loading, Error
}

@Composable
fun DetailRequestPage(
    state: DetailRequestState,
    assetTitle: String,
    assetId: String,
    borrowDate: String,
    returnEstimate: String,
    message: String,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onApproveClick: () -> Unit,
    onTolakClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Detail Request",
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
        if (state == DetailRequestState.Error) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = null,
                    tint = EnuTheme.colors.contentSignalErrorDefault,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Terjadi Kesalahan",
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )
                Text(
                    text = "error log",
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentSignalErrorDefault,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                EnuButton(
                    text = "Coba lagi",
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                EnuDetailHistoryCard(
                    title = assetTitle,
                    id = assetId
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
                    border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tanggal pinjam",
                                    style = EnuTheme.typography.content.headings.h6,
                                    color = EnuTheme.colors.contentDefaultSubtle
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = borrowDate,
                                    style = EnuTheme.typography.ui.labels.normalCase.base,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Estimasi Kembali",
                                    style = EnuTheme.typography.content.headings.h6,
                                    color = EnuTheme.colors.contentDefaultSubtle
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = returnEstimate,
                                    style = EnuTheme.typography.ui.labels.normalCase.base,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Pesan",
                                style = EnuTheme.typography.content.headings.h6,
                                color = EnuTheme.colors.contentDefaultSubtle
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message,
                                style = EnuTheme.typography.ui.labels.normalCase.base,
                                color = EnuTheme.colors.contentDefaultPrimary
                            )
                        }
                    }
                }

                val approveButtonVariant = if (state == DetailRequestState.Loading) {
                    EnuButtonVariant.Loading
                } else {
                    EnuButtonVariant.Normal
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnuButton(
                        text = "Approve",
                        variant = approveButtonVariant,
                        onClick = onApproveClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    EnuButton(
                        text = "Tolak",
                        variant = EnuButtonVariant.Danger,
                        onClick = onTolakClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun DetailRequestPagePreviewLight() {
    EnuTheme {
        DetailRequestPage(
            state = DetailRequestState.Normal,
            assetTitle = "Arduino Micro Controller",
            assetId = "HW-0019-A",
            borrowDate = "16 Okt 26",
            returnEstimate = "16 Okt 26",
            message = "Pak saya butuh alat ini pls pinjemin saya",
            currentRoute = "approval",
            onBottomBarItemClick = {},
            onBackClick = {},
            onApproveClick = {},
            onTolakClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun DetailRequestPagePreviewDark() {
    EnuTheme(darkTheme = true) {
        DetailRequestPage(
            state = DetailRequestState.Loading,
            assetTitle = "Arduino Micro Controller",
            assetId = "HW-0019-A",
            borrowDate = "16 Okt 26",
            returnEstimate = "16 Okt 26",
            message = "Pak saya butuh alat ini pls pinjemin saya",
            currentRoute = "approval",
            onBottomBarItemClick = {},
            onBackClick = {},
            onApproveClick = {},
            onTolakClick = {},
            onRetryClick = {}
        )
    }
}