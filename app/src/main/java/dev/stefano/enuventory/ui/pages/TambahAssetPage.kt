package dev.stefano.enuventory.ui.pages

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuTextField
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.common.EnuErrorState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahAssetPage(
    state: UiState<Unit>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onAddPhotoClick: () -> Unit,
    onTambahAssetClick: (title: String, stock: String, status: String, category: String, description: String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

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
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    var statusInput by remember { mutableStateOf("") }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Tersedia", "Tidak Tersedia", "Maintenance")

    var categoryInput by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }
    val existingCategories = remember { mutableStateListOf("Elektro", "IoT", "Mekanik") }

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
                        existingCategories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        categoryInput = cat
                                        showCategoryDialog = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
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
                                existingCategories.add(newCategoryInput.trim())
                                categoryInput = newCategoryInput.trim()
                                newCategoryInput = ""
                                showCategoryDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Tambah Asset",
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
        if (state is UiState.Error) {
            EnuErrorState(
                errorMessage = state.message,
                onRetryClick = onRetryClick,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
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
                            if (bitmap == null) {
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
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
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

                EnuTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = "e.g Macbook Pro 14\"",
                    label = "Title",
                    isRequired = true
                )

                EnuTextField(
                    value = stockInput,
                    onValueChange = { stockInput = it },
                    placeholder = "Enter stock",
                    label = "Stock",
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
                        modifier = Modifier.clickable { isStatusDropdownExpanded = true }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 24.dp)
                            .clickable { isStatusDropdownExpanded = true }
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
                        trailingIcon = R.drawable.ic_down
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 24.dp)
                            .clickable { showCategoryDialog = true }
                    )
                }

                EnuTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    placeholder = "Optional description..",
                    label = "Description"
                )

                val buttonVariant =
                    if (state is UiState.Loading) EnuButtonVariant.Loading else EnuButtonVariant.Normal

                EnuButton(
                    text = "Tambah Asset",
                    variant = buttonVariant,
                    onClick = {
                        onTambahAssetClick(
                            titleInput,
                            stockInput,
                            statusInput,
                            categoryInput,
                            descriptionInput
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun TambahAssetPagePreviewLight() {
    EnuTheme {
        TambahAssetPage(
            state = UiState.Success(Unit),
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onAddPhotoClick = {},
            onTambahAssetClick = { _, _, _, _, _ -> },
            onRetryClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun TambahAssetPagePreviewDark() {
    EnuTheme(darkTheme = true) {
        TambahAssetPage(
            state = UiState.Loading,
            currentRoute = "home",
            onBottomBarItemClick = {},
            onBackClick = {},
            onAddPhotoClick = {},
            onTambahAssetClick = { _, _, _, _, _ -> },
            onRetryClick = {}
        )
    }
}