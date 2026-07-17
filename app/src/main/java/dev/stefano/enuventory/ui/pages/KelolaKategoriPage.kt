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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuConfirmationDialog
import dev.stefano.enuventory.ui.components.EnuTextField
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.screen.category.CategoryUi
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun KelolaKategoriPage(
    categoriesState: UiState<List<CategoryUi>>,
    actionError: String?,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onAddCategory: (name: String, onSuccess: () -> Unit) -> Unit,
    onRenameCategory: (category: CategoryUi, newName: String, onSuccess: () -> Unit) -> Unit,
    onDeleteCategory: (category: CategoryUi, onSuccess: () -> Unit) -> Unit,
    onClearActionError: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryBeingEdited by remember { mutableStateOf<CategoryUi?>(null) }
    var categoryPendingDelete by remember { mutableStateOf<CategoryUi?>(null) }

    if (showAddDialog) {
        CategoryNameDialog(
            title = "Tambah Kategori",
            initialName = "",
            confirmText = "Tambah",
            onConfirm = { name -> onAddCategory(name) { showAddDialog = false } },
            onDismissRequest = { showAddDialog = false }
        )
    }

    categoryBeingEdited?.let { categoryUi ->
        CategoryNameDialog(
            title = "Edit Kategori",
            initialName = categoryUi.category.name,
            confirmText = "Simpan",
            onConfirm = { newName ->
                onRenameCategory(categoryUi, newName) { categoryBeingEdited = null }
            },
            onDismissRequest = { categoryBeingEdited = null }
        )
    }

    categoryPendingDelete?.let { categoryUi ->
        if (categoryUi.usageCount > 0) {
            CategoryInUseDialog(
                categoryUi = categoryUi,
                onDismissRequest = { categoryPendingDelete = null }
            )
        } else {
            EnuConfirmationDialog(
                title = "Hapus Kategori",
                message = "Kategori \"${categoryUi.category.name}\" akan dihapus permanen. Lanjutkan?",
                onConfirmClick = {
                    onDeleteCategory(categoryUi) { categoryPendingDelete = null }
                },
                onDismissRequest = { categoryPendingDelete = null }
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Kelola Kategori",
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
                .padding(horizontal = 24.dp)
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
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Tutup",
                            style = EnuTheme.typography.ui.labels.normalCase.small,
                            color = EnuTheme.colors.contentSignalErrorDefault,
                            modifier = Modifier.clickable { onClearActionError() }
                        )
                    }
                }
            }

            EnuButton(
                text = "+ Tambah Kategori",
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (categoriesState) {
                is UiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categoriesState.data, key = { it.category.id }) { categoryUi ->
                            CategoryRow(
                                categoryUi = categoryUi,
                                onEditClick = { categoryBeingEdited = categoryUi },
                                onDeleteClick = { categoryPendingDelete = categoryUi }
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
                    EnuErrorState(
                        errorMessage = categoriesState.message,
                        onRetryClick = onRetryClick
                    )
                }

                is UiState.Empty -> {
                    EnuEmptyState(message = "Belum ada kategori. Tambah kategori pertama kamu.")
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categoryUi: CategoryUi,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryUi.category.name,
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )
                Text(
                    text = "${categoryUi.usageCount} asset",
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentDefaultSubtle
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Edit",
                    style = EnuTheme.typography.ui.labels.normalCase.base,
                    color = EnuTheme.colors.contentBrandPrimaryDefault,
                    modifier = Modifier.clickable(onClick = onEditClick)
                )
                Text(
                    text = "Hapus",
                    style = EnuTheme.typography.ui.labels.normalCase.base,
                    color = EnuTheme.colors.contentSignalErrorDefault,
                    modifier = Modifier.clickable(onClick = onDeleteClick)
                )
            }
        }
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialName: String,
    confirmText: String,
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
                    text = title,
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )

                EnuTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "Nama kategori"
                )

                EnuButton(
                    text = confirmText,
                    onClick = { if (nameInput.isNotBlank()) onConfirm(nameInput) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CategoryInUseDialog(
    categoryUi: CategoryUi,
    onDismissRequest: () -> Unit
) {
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tidak Bisa Dihapus",
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Kategori \"${categoryUi.category.name}\" masih dipakai " +
                            "${categoryUi.usageCount} asset. Pindahin dulu kategori asset-nya " +
                            "sebelum menghapus kategori ini.",
                    style = EnuTheme.typography.ui.labels.normalCase.base,
                    color = EnuTheme.colors.contentDefaultSubtle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                EnuButton(
                    text = "Mengerti",
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun KelolaKategoriPagePreviewLight() {
    val dummyCategories = listOf(
        CategoryUi(Category(id = "1", name = "Elektro"), usageCount = 3),
        CategoryUi(Category(id = "2", name = "IoT"), usageCount = 0)
    )
    EnuTheme {
        KelolaKategoriPage(
            categoriesState = UiState.Success(dummyCategories),
            actionError = null,
            currentRoute = "settings",
            onBottomBarItemClick = {},
            onBackClick = {},
            onAddCategory = { _, _ -> },
            onRenameCategory = { _, _, _ -> },
            onDeleteCategory = { _, _ -> },
            onClearActionError = {},
            onRetryClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun KelolaKategoriPagePreviewDark() {
    val dummyCategories = listOf(
        CategoryUi(Category(id = "1", name = "Elektro"), usageCount = 3),
        CategoryUi(Category(id = "2", name = "IoT"), usageCount = 0)
    )
    EnuTheme(darkTheme = true) {
        KelolaKategoriPage(
            categoriesState = UiState.Success(dummyCategories),
            actionError = "Gagal menghapus kategori",
            currentRoute = "settings",
            onBottomBarItemClick = {},
            onBackClick = {},
            onAddCategory = { _, _ -> },
            onRenameCategory = { _, _, _ -> },
            onDeleteCategory = { _, _ -> },
            onClearActionError = {},
            onRetryClick = {}
        )
    }
}
