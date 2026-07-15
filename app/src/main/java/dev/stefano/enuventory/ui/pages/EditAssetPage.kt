package dev.stefano.enuventory.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import dev.stefano.enuventory.R
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuTextField
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme

private fun AssetStatus.toStatusLabel(): String = when (this) {
    AssetStatus.Available -> "Tersedia"
    AssetStatus.Reserved -> "Direservasi"
    AssetStatus.Maintenance -> "Maintenance"
}

@Composable
fun EditAssetPage(
    assetState: UiState<Asset>,
    saveState: UiState<Unit>,
    categories: List<Category>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onEditAssetClick: (
        title: String,
        status: String,
        category: String,
        description: String,
        imageBytes: ByteArray?
    ) -> Unit,
    onAddCategory: (name: String, onSuccess: (String) -> Unit) -> Unit,
    onManageCategoriesClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Edit Asset",
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
        when (assetState) {
            is UiState.Success -> {
                EditAssetForm(
                    asset = assetState.data,
                    saveState = saveState,
                    categories = categories,
                    onEditAssetClick = onEditAssetClick,
                    onAddCategory = onAddCategory,
                    onManageCategoriesClick = onManageCategoriesClick,
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
                    errorMessage = assetState.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is UiState.Empty -> {
                EnuEmptyState(
                    message = "Asset tidak ditemukan",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAssetForm(
    asset: Asset,
    saveState: UiState<Unit>,
    categories: List<Category>,
    onEditAssetClick: (
        title: String,
        status: String,
        category: String,
        description: String,
        imageBytes: ByteArray?
    ) -> Unit,
    onAddCategory: (name: String, onSuccess: (String) -> Unit) -> Unit,
    onManageCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleInput by remember(asset.id) { mutableStateOf(asset.title) }
    var descriptionInput by remember(asset.id) { mutableStateOf(asset.description) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    val bitmap = remember(imageUri) {
        imageUri?.let { uri ->
            try {
                if (android.os.Build.VERSION.SDK_INT < 28) {
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source =
                        android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    var statusInput by remember(asset.id) { mutableStateOf(asset.status.toStatusLabel()) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Tersedia", "Direservasi", "Maintenance")

    var categoryInput by remember(asset.id) { mutableStateOf(asset.category) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }
    // Safety net: kalau kategori asset ini (mis. data lama) gak ada di daftar kategori
    // yang beneran terdaftar, tetep tampilin biar gak "hilang" dari pilihan.
    val displayCategories = remember(categories, asset.category) {
        if (asset.category.isNotBlank() && categories.none { it.name == asset.category }) {
            categories + Category(id = "", name = asset.category)
        } else {
            categories
        }
    }

    val borderColor = EnuTheme.colors.borderDefaultMedium

    if (showCategoryDialog) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
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
                        text = "Pilih atau Tambah Kategori",
                        style = EnuTheme.typography.ui.labels.normalCase.large,
                        color = EnuTheme.colors.contentDefaultPrimary
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        displayCategories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        categoryInput = cat.name
                                        showCategoryDialog = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    style = EnuTheme.typography.ui.labels.normalCase.base,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = EnuTheme.colors.borderDefaultMedium)

                    EnuTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        placeholder = "Buat kategori baru..."
                    )

                    EnuButton(
                        text = "Tambah & Pilih Kategori",
                        onClick = {
                            if (newCategoryInput.isNotBlank()) {
                                onAddCategory(newCategoryInput.trim()) { addedName ->
                                    categoryInput = addedName
                                }
                                newCategoryInput = ""
                                showCategoryDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Kelola Kategori",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentBrandPrimaryDefault,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCategoryDialog = false
                                onManageCategoriesClick()
                            }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { photoPickerLauncher.launch("image/*") }
                .drawBehind {
                    if (bitmap == null && asset.imageUrl == null) {
                        drawRoundRect(
                            color = borderColor,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(10f, 10f),
                                    phase = 0f
                                )
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                bitmap != null -> {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                asset.imageUrl != null -> {
                    AsyncImage(
                        model = asset.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = EnuTheme.colors.contentDefaultPrimary
                        )
                        Text(
                            text = "Add Photo",
                            style = EnuTheme.typography.ui.labels.normalCase.large,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                        Text(
                            text = "optional",
                            style = EnuTheme.typography.ui.labels.normalCase.small,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                    }
                }
            }
        }

        EnuTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            placeholder = "e.g Macbook Pro 14\"",
            label = "Title",
            isRequired = true
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            EnuTextField(
                value = statusInput,
                onValueChange = {},
                placeholder = "Enter Status",
                label = "Status",
                isRequired = true,
                trailingIcon = R.drawable.ic_down,
                readOnly = true,
                onClick = { isStatusDropdownExpanded = true }
            )
            DropdownMenu(
                expanded = isStatusDropdownExpanded,
                onDismissRequest = { isStatusDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(EnuTheme.colors.surfaceDefaultBase)
            ) {
                statusOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = EnuTheme.typography.ui.labels.normalCase.base,
                                color = EnuTheme.colors.contentDefaultPrimary
                            )
                        },
                        onClick = {
                            statusInput = option
                            isStatusDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            EnuTextField(
                value = categoryInput,
                onValueChange = {},
                placeholder = "Optional Category",
                label = "Category",
                trailingIcon = R.drawable.ic_down,
                readOnly = true,
                onClick = { showCategoryDialog = true }
            )
        }

        EnuTextField(
            value = descriptionInput,
            onValueChange = { descriptionInput = it },
            placeholder = "Optional description..",
            label = "Description"
        )

        if (validationError != null) {
            Text(
                text = validationError.orEmpty(),
                style = EnuTheme.typography.ui.labels.normalCase.small,
                color = EnuTheme.colors.contentSignalErrorDefault
            )
        }

        if (saveState is UiState.Error) {
            Text(
                text = saveState.message,
                style = EnuTheme.typography.ui.labels.normalCase.small,
                color = EnuTheme.colors.contentSignalErrorDefault
            )
        }

        val buttonVariant =
            if (saveState is UiState.Loading) EnuButtonVariant.Loading else EnuButtonVariant.Normal

        EnuButton(
            text = "Edit Asset",
            variant = buttonVariant,
            onClick = {
                validationError = when {
                    titleInput.isBlank() -> "Title wajib diisi"
                    statusInput.isBlank() -> "Status wajib dipilih"
                    else -> null
                }

                if (validationError == null) {
                    val imageBytes = bitmap?.let { bmp ->
                        java.io.ByteArrayOutputStream().use { stream ->
                            bmp.compress(
                                android.graphics.Bitmap.CompressFormat.JPEG,
                                80,
                                stream
                            )
                            stream.toByteArray()
                        }
                    }
                    onEditAssetClick(
                        titleInput,
                        statusInput,
                        categoryInput,
                        descriptionInput,
                        imageBytes
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EditAssetPagePreviewLight() {
    val dummyAsset = Asset(
        id = "HW-001",
        title = "Macbook Pro 14",
        status = AssetStatus.Available,
        category = "Elektro",
        description = "Laptop untuk programming"
    )
    EnuTheme {
        EditAssetPage(
            assetState = UiState.Success(dummyAsset),
            saveState = UiState.Success(Unit),
            categories = emptyList(),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onEditAssetClick = { _, _, _, _, _ -> },
            onAddCategory = { _, _ -> },
            onManageCategoriesClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun EditAssetPagePreviewDark() {
    val dummyAsset = Asset(
        id = "HW-001",
        title = "Macbook Pro 14",
        status = AssetStatus.Available,
        category = "Elektro",
        description = "Laptop untuk programming"
    )
    EnuTheme(darkTheme = true) {
        EditAssetPage(
            assetState = UiState.Success(dummyAsset),
            saveState = UiState.Loading,
            categories = emptyList(),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onEditAssetClick = { _, _, _, _, _ -> },
            onAddCategory = { _, _ -> },
            onManageCategoriesClick = {},
            onRetryClick = {}
        )
    }
}
