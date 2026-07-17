package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.data.dummyAssets
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuCategoryBadge
import dev.stefano.enuventory.ui.components.EnuCategoryBadgeState
import dev.stefano.enuventory.ui.components.EnuInventoryCard
import dev.stefano.enuventory.ui.components.EnuInventoryStatus
import dev.stefano.enuventory.ui.components.EnuSearchField
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.toUiStatus

@Composable
fun HomeUserPage(
    state: UiState<List<Asset>>,
    categories: List<String>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onRetryClick: () -> Unit,
    onAssetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Home",
                showNotification = true,
                notificationCount = notificationCount,
                onNotificationClick = onNotificationClick
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
            if (state !is UiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))

                EnuSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.size) { index ->
                        val badgeState = if (state is UiState.Loading) {
                            EnuCategoryBadgeState.Loading
                        } else if (index == selectedCategoryIndex) {
                            EnuCategoryBadgeState.Selected
                        } else {
                            EnuCategoryBadgeState.Unselected
                        }

                        EnuCategoryBadge(
                            text = categories[index],
                            state = badgeState,
                            onClick = { selectedCategoryIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            when (state) {
                is UiState.Success -> {
                    val selectedCategory = categories.getOrElse(selectedCategoryIndex) {
                        categories.firstOrNull() ?: "All"
                    }
                    val filteredAssets = remember(state.data, selectedCategory, searchQuery) {
                        state.data.filter { item ->
                            val matchesCategory =
                                selectedCategoryIndex == 0 || item.category.lowercase() == selectedCategory.lowercase()
                            val matchesSearch = searchQuery.isBlank() || item.title.lowercase().contains(searchQuery.lowercase()) || item.id.lowercase().contains(searchQuery.lowercase())
                            matchesCategory && matchesSearch
                        }
                    }
                    if (filteredAssets.isEmpty()) {
                        EnuEmptyState("Aset tidak ditemukan")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredAssets) { item ->
                                EnuInventoryCard(
                                    title = item.title,
                                    id = item.id,
                                    status = item.status.toUiStatus(),
                                    imageUrl = item.imageUrl,
                                    modifier = Modifier.clickable { onAssetClick(item.id) }
                                )
                            }
                        }
                    }
                }

                is UiState.Loading -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(3) {
                            EnuInventoryCard(
                                title = "", id = "",
                                status = EnuInventoryStatus.Tersedia,
                                isLoading = true
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    EnuErrorState(errorMessage = state.message, onRetryClick = onRetryClick)
                }

                is UiState.Empty -> {
                    EnuEmptyState(message = "Belum ada asset yang ditambahkan")
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeUserPageNormalPreviewLight() {
    EnuTheme {
        HomeUserPage(
            state = UiState.Success(dummyAssets),
            categories = listOf("All", "Elektro", "IoT"),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onAssetClick = {}
        )
    }
}

@Preview
@Composable
fun HomeUserPageLoadingPreviewLight() {
    EnuTheme {
        HomeUserPage(
            state = UiState.Loading,
            categories = listOf("All", "Elektro", "IoT"),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onAssetClick = {}
        )
    }
}

@Preview
@Composable
fun HomeUserPageErrorPreviewLight() {
    EnuTheme {
        HomeUserPage(
            state = UiState.Error("Gagal memuat data"),
            categories = listOf("All", "Elektro", "IoT"),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onAssetClick = {}
        )
    }
}

@Preview
@Composable
fun HomeUserPageEmptyPreviewLight() {
    EnuTheme {
        HomeUserPage(
            state = UiState.Empty,
            categories = listOf("All", "Elektro", "IoT"),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onAssetClick = {}
        )
    }
}

@Preview
@Composable
fun HomeUserPageNormalPreviewDark() {
    EnuTheme(darkTheme = true) {
        HomeUserPage(
            state = UiState.Success(dummyAssets),
            categories = listOf("All", "Elektro", "IoT"),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onRetryClick = {},
            onAssetClick = {}
        )
    }
}