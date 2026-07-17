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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.R
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuTextField
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun KelolaUserPage(
    usersState: UiState<List<User>>,
    actionError: String?,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onUserClick: (uid: String) -> Unit,
    onAddUser: (name: String, email: String, password: String, onSuccess: () -> Unit) -> Unit,
    onClearActionError: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddUserDialog(
            onConfirm = { name, email, password ->
                onAddUser(name, email, password) { showAddDialog = false }
            },
            onDismissRequest = { showAddDialog = false }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Kelola User",
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
                text = "+ Tambah User",
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (usersState) {
                is UiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(usersState.data, key = { it.uid }) { user ->
                            UserRow(user = user, onClick = { onUserClick(user.uid) })
                        }
                    }
                }

                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EnuTheme.colors.contentBrandPrimaryDefault)
                    }
                }

                is UiState.Error -> {
                    EnuErrorState(errorMessage = usersState.message, onRetryClick = onRetryClick)
                }

                is UiState.Empty -> {
                    EnuEmptyState(message = "Belum ada akun user. Tambah user pertama kamu.")
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: User,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { "(Tanpa nama)" },
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )
                Text(
                    text = user.email,
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentDefaultSubtle
                )
            }

            if (user.disabled) {
                Text(
                    text = "Nonaktif",
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentSignalErrorDefault
                )
            }
        }
    }
}

@Composable
private fun AddUserDialog(
    onConfirm: (name: String, email: String, password: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                    text = "Tambah User",
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )

                EnuTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "Nama lengkap",
                    label = "Nama",
                    isRequired = true
                )

                EnuTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    placeholder = "email@contoh.com",
                    label = "Email",
                    isRequired = true,
                    leadingIcon = R.drawable.ic_email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                EnuTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    placeholder = "Minimal 6 karakter",
                    label = "Password",
                    isRequired = true,
                    leadingIcon = R.drawable.ic_lock,
                    trailingIcon = if (passwordVisible) R.drawable.ic_opened_eye else R.drawable.ic_closed_eye,
                    onTrailingIconClick = { passwordVisible = !passwordVisible },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (validationError != null) {
                    Text(
                        text = validationError.orEmpty(),
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentSignalErrorDefault
                    )
                }

                EnuButton(
                    text = "Tambah",
                    onClick = {
                        validationError = when {
                            nameInput.isBlank() -> "Nama wajib diisi"
                            emailInput.isBlank() -> "Email wajib diisi"
                            passwordInput.length < 6 -> "Password minimal 6 karakter"
                            else -> null
                        }
                        if (validationError == null) {
                            onConfirm(nameInput, emailInput, passwordInput)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun KelolaUserPagePreviewLight() {
    val dummyUsers = listOf(
        User(
            uid = "1",
            name = "Budi Santoso",
            email = "budi@example.com",
            role = UserRole.RegularUser
        ),
        User(
            uid = "2",
            name = "Siti Aminah",
            email = "siti@example.com",
            role = UserRole.RegularUser,
            disabled = true
        )
    )
    EnuTheme {
        KelolaUserPage(
            usersState = UiState.Success(dummyUsers),
            actionError = null,
            currentRoute = "settings",
            onBottomBarItemClick = {},
            onBackClick = {},
            onUserClick = {},
            onAddUser = { _, _, _, _ -> },
            onClearActionError = {},
            onRetryClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun KelolaUserPagePreviewDark() {
    val dummyUsers = listOf(
        User(
            uid = "1",
            name = "Budi Santoso",
            email = "budi@example.com",
            role = UserRole.RegularUser
        )
    )
    EnuTheme(darkTheme = true) {
        KelolaUserPage(
            usersState = UiState.Success(dummyUsers),
            actionError = "Email sudah dipakai",
            currentRoute = "settings",
            onBottomBarItemClick = {},
            onBackClick = {},
            onUserClick = {},
            onAddUser = { _, _, _, _ -> },
            onClearActionError = {},
            onRetryClick = {}
        )
    }
}
