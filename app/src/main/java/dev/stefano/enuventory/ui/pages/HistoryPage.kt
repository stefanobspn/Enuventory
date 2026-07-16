package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.data.dummyBorrowRecords
import dev.stefano.enuventory.ui.screen.history.HistoryItemUiModel
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBorrowStatus
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuHistoryCard
import dev.stefano.enuventory.ui.components.EnuTab
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.formatDate
import dev.stefano.enuventory.ui.util.toUiStatus

@Composable
fun HistoryPage(
    state: UiState<List<HistoryItemUiModel>>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onRetryClick: () -> Unit,
    onDetailClick: (id: String) -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    val tabTitles = listOf("Aktif", "Selesai")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "History",
                showNotification = false
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            EnuTab(
                tabs = tabTitles,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (state) {
                is UiState.Success -> {
                    val filteredItems = remember(state.data, selectedTabIndex) {
                        state.data.filter { it.record.isFinished == (selectedTabIndex == 1) }
                    }
                    if (filteredItems.isEmpty()) {
                        EnuEmptyState("Belum ada riwayat")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredItems) { item ->
                                val record = item.record
                                EnuHistoryCard(
                                    title = record.assetTitle,
                                    id = record.assetId,
                                    status = record.toUiStatus(),
                                    borrowDate = formatDate(record.borrowDate),
                                    returnEstimate = if (record.isFinished) {
                                        record.returnDate?.let(::formatDate) ?: "-"
                                    } else {
                                        formatDate(record.returnEstimate)
                                    },
                                    isFinished = record.isFinished,
                                    imageUrl = item.imageUrl,
                                    onDetailClick = { onDetailClick(record.id) }
                                )
                            }
                        }
                    }
                }

                is UiState.Loading -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(2) {
                            EnuHistoryCard(
                                title = "", id = "",
                                status = EnuBorrowStatus.Menunggu,
                                borrowDate = "", returnEstimate = "",
                                onDetailClick = {},
                                isLoading = true
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    EnuErrorState(errorMessage = state.message, onRetryClick = onRetryClick)
                }

                is UiState.Empty -> {
                    EnuEmptyState(message = "Belum ada riwayat")
                }
            }
        }
    }
}

@Preview(name = "Light Normal")
@Composable
fun HistoryPageNormalPreviewLight() {
    EnuTheme {
        HistoryPage(
            state = UiState.Success(emptyList()), // Empty list for preview since dummyBorrowRecords doesn't match HistoryItemUiModel directly
            currentRoute = "history",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onDetailClick = {}
        )
    }
}

@Preview(name = "Light Loading")
@Composable
fun HistoryPageLoadingPreviewLight() {
    EnuTheme {
        HistoryPage(
            state = UiState.Loading,
            currentRoute = "history",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onDetailClick = {}
        )
    }
}

@Preview(name = "Light Error")
@Composable
fun HistoryPageErrorPreviewLight() {
    EnuTheme {
        HistoryPage(
            state = UiState.Error("Terjadi kesalahan memuat data"),
            currentRoute = "history",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onDetailClick = {}
        )
    }
}

@Preview(name = "Light Empty")
@Composable
fun HistoryPageEmptyPreviewLight() {
    EnuTheme {
        HistoryPage(
            state = UiState.Empty,
            currentRoute = "history",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onDetailClick = {}
        )
    }
}

@Preview(name = "Dark Normal")
@Composable
fun HistoryPageNormalPreviewDark() {
    EnuTheme(darkTheme = true) {
        HistoryPage(
            state = UiState.Success(emptyList()), // Empty list for preview
            currentRoute = "history",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onDetailClick = {}
        )
    }
}
