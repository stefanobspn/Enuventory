package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.screen.notification.NotificationItem
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun NotificationPage(
    state: UiState<List<NotificationItem>>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (id: String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Notifikasi",
                showBack = true,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            EnuBottomBar(
                isAdmin = isAdmin,
                currentRoute = currentRoute,
                onItemClick = onBottomBarItemClick
            )
        },
        containerColor = EnuTheme.colors.surfaceDefaultBase
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (state) {
                is UiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(state.data, key = { it.id }) { notification ->
                            NotificationRow(
                                notification = notification,
                                onClick = { onItemClick(notification.id) }
                            )
                        }
                    }
                }

                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EnuTheme.colors.contentBrandPrimaryDefault)
                    }
                }

                is UiState.Error -> {
                    EnuErrorState(errorMessage = state.message, onRetryClick = onRetryClick)
                }

                is UiState.Empty -> {
                    EnuEmptyState(message = "Belum ada notifikasi")
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = notification.title,
                style = EnuTheme.typography.ui.labels.normalCase.large,
                color = EnuTheme.colors.contentDefaultPrimary
            )
            Text(
                text = notification.message,
                style = EnuTheme.typography.ui.labels.normalCase.small,
                color = EnuTheme.colors.contentDefaultSubtle
            )
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun NotificationPagePreviewLight() {
    val dummyNotifications = listOf(
        NotificationItem(
            id = "1",
            title = "Permintaan Peminjaman Baru",
            message = "Budi Santoso mengajukan pinjam \"Macbook Pro 14\""
        ),
        NotificationItem(
            id = "2",
            title = "Permintaan Peminjaman Baru",
            message = "Siti Aminah mengajukan pinjam \"Proyektor Epson\""
        )
    )
    EnuTheme {
        NotificationPage(
            state = UiState.Success(dummyNotifications),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onItemClick = {},
            onRetryClick = {},
            isAdmin = true
        )
    }
}

@Preview(name = "Dark - Empty")
@Composable
fun NotificationPageEmptyPreviewDark() {
    EnuTheme(darkTheme = true) {
        NotificationPage(
            state = UiState.Empty,
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onItemClick = {},
            onRetryClick = {}
        )
    }
}
