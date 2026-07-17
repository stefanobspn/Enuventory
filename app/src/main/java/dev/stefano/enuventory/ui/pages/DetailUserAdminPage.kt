package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBorrowStatus
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuConfirmationDialog
import dev.stefano.enuventory.ui.components.EnuHistoryCard
import dev.stefano.enuventory.ui.components.EnuTab
import dev.stefano.enuventory.ui.components.EnuTextField
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.formatDate
import dev.stefano.enuventory.ui.util.toUiStatus

@Composable
fun DetailUserAdminPage(
    userState: UiState<User>,
    historyState: UiState<List<BorrowRecord>>,
    actionError: String?,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onRenameUser: (name: String, onSuccess: () -> Unit) -> Unit,
    onSetDisabled: (disabled: Boolean, onSuccess: () -> Unit) -> Unit,
    onClearActionError: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Detail User",
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
        when (userState) {
            is UiState.Success -> {
                DetailUserAdminContent(
                    user = userState.data,
                    historyState = historyState,
                    actionError = actionError,
                    onRenameUser = onRenameUser,
                    onSetDisabled = onSetDisabled,
                    onClearActionError = onClearActionError,
                    onHistoryRetryClick = onRetryClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EnuTheme.colors.contentBrandPrimaryDefault)
                }
            }

            is UiState.Error -> {
                EnuErrorState(
                    errorMessage = userState.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is UiState.Empty -> {
                EnuEmptyState(
                    message = "User tidak ditemukan",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun DetailUserAdminContent(
    user: User,
    historyState: UiState<List<BorrowRecord>>,
    actionError: String?,
    onRenameUser: (name: String, onSuccess: () -> Unit) -> Unit,
    onSetDisabled: (disabled: Boolean, onSuccess: () -> Unit) -> Unit,
    onClearActionError: () -> Unit,
    onHistoryRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDisableConfirmation by remember { mutableStateOf(false) }
    val tabTitles = listOf("Aktif", "Selesai")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    if (showRenameDialog) {
        RenameUserDialog(
            initialName = user.name,
            onConfirm = { newName -> onRenameUser(newName) { showRenameDialog = false } },
            onDismissRequest = { showRenameDialog = false }
        )
    }

    if (showDisableConfirmation) {
        EnuConfirmationDialog(
            title = "Nonaktifkan Akun",
            message = "\"${user.name}\" gak akan bisa login lagi sampai diaktifkan ulang. Lanjutkan?",
            onConfirmClick = {
                onSetDisabled(true) { showDisableConfirmation = false }
            },
            onDismissRequest = { showDisableConfirmation = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (actionError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EnuTheme.colors.backgroundSignalErrorMediumDefault
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionError,
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentSignalErrorDefault,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Tutup",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentSignalErrorDefault,
                        modifier = Modifier.clickable { onClearActionError() }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = user.name.ifBlank { "(Tanpa nama)" },
                    style = EnuTheme.typography.content.headings.h3,
                    color = EnuTheme.colors.contentDefaultPrimary
                )
                Text(
                    text = user.email,
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentDefaultSubtle
                )
                if (user.disabled) {
                    Text(
                        text = "Akun nonaktif",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentSignalErrorDefault
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EnuButton(
                text = "Edit Nama",
                onClick = { showRenameDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
            if (user.disabled) {
                EnuButton(
                    text = "Aktifkan Akun",
                    onClick = { onSetDisabled(false) {} },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EnuButton(
                    text = "Nonaktifkan Akun",
                    onClick = { showDisableConfirmation = true },
                    variant = EnuButtonVariant.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Riwayat Peminjaman",
            style = EnuTheme.typography.content.headings.h3,
            color = EnuTheme.colors.contentDefaultPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        EnuTab(
            tabs = tabTitles,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (historyState) {
            is UiState.Success -> {
                val filteredItems = historyState.data.filter {
                    it.isFinished == (selectedTabIndex == 1)
                }
                if (filteredItems.isEmpty()) {
                    EnuEmptyState("Belum ada riwayat")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        filteredItems.forEach { item ->
                            EnuHistoryCard(
                                title = item.assetTitle,
                                id = item.assetId,
                                status = item.toUiStatus(),
                                borrowDate = formatDate(item.borrowDate),
                                returnEstimate = if (item.isFinished) {
                                    item.returnDate?.let(::formatDate) ?: "-"
                                } else {
                                    formatDate(item.returnEstimate)
                                },
                                isFinished = item.isFinished,
                                onDetailClick = {}
                            )
                        }
                    }
                }
            }

            is UiState.Loading -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(2) {
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
                EnuErrorState(
                    errorMessage = historyState.message,
                    onRetryClick = onHistoryRetryClick
                )
            }

            is UiState.Empty -> {
                EnuEmptyState(message = "Belum ada riwayat")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RenameUserDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismissRequest) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Nama",
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )

                EnuTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "Nama lengkap"
                )

                EnuButton(
                    text = "Simpan",
                    onClick = { if (nameInput.isNotBlank()) onConfirm(nameInput) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun DetailUserAdminPagePreviewLight() {
    val dummyUser = User(
        uid = "1",
        name = "Budi Santoso",
        email = "budi@example.com",
        role = UserRole.RegularUser
    )
    val dummyHistory = listOf(
        BorrowRecord(
            id = "r1",
            assetId = "HW-001",
            assetTitle = "Macbook Pro 14",
            borrowerId = "1",
            borrowerName = "Budi Santoso",
            status = BorrowStatus.Borrowed,
            requestedAt = 1_784_246_400_000L,
            borrowDate = 1_784_246_400_000L,
            returnEstimate = 1_784_851_200_000L,
            reason = "Kebutuhan proyek"
        )
    )
    EnuTheme {
        DetailUserAdminPage(
            userState = UiState.Success(dummyUser),
            historyState = UiState.Success(dummyHistory),
            actionError = null,
            currentRoute = "settings",
            onBottomBarItemClick = {},
            onBackClick = {},
            onRenameUser = { _, _ -> },
            onSetDisabled = { _, _ -> },
            onClearActionError = {},
            onRetryClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun DetailUserAdminPagePreviewDark() {
    val dummyUser = User(
        uid = "1",
        name = "Budi Santoso",
        email = "budi@example.com",
        role = UserRole.RegularUser,
        disabled = true
    )
    EnuTheme(darkTheme = true) {
        DetailUserAdminPage(
            userState = UiState.Success(dummyUser),
            historyState = UiState.Empty,
            actionError = null,
            currentRoute = "settings",
            onBottomBarItemClick = {},
            onBackClick = {},
            onRenameUser = { _, _ -> },
            onSetDisabled = { _, _ -> },
            onClearActionError = {},
            onRetryClick = {}
        )
    }
}
