package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.domain.model.AppThemeMode
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuConfirmationDialog
import dev.stefano.enuventory.ui.components.EnuThemeSelector
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun SettingsAdminPage(
    username: String,
    role: String,
    appVersion: String,
    currentRoute: String?,
    selectedTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onSignOutClick: () -> Unit,
    onKelolaKategoriClick: () -> Unit,
    onKelolaUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    var showSignOutConfirmation by remember { mutableStateOf(false) }

    if (showSignOutConfirmation) {
        EnuConfirmationDialog(
            title = "Sign Out",
            message = "Kamu yakin ingin keluar dari akun ini?",
            onConfirmClick = {
                showSignOutConfirmation = false
                onSignOutClick()
            },
            onDismissRequest = { showSignOutConfirmation = false }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(title = "Settings", showNotification = false)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ACCOUNT",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentDefaultSubtle
                    )

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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(EnuTheme.colors.backgroundNeutralMediumDefault),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_user),
                                    contentDescription = "User Avatar",
                                    tint = EnuTheme.colors.contentBrandPrimaryDefault
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = username,
                                    style = EnuTheme.typography.ui.labels.normalCase.large,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                                Text(
                                    text = role,
                                    style = EnuTheme.typography.ui.labels.normalCase.small,
                                    color = EnuTheme.colors.contentDefaultSubtle
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INVENTORY",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentDefaultSubtle
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onKelolaKategoriClick),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_settings),
                                    contentDescription = "Kelola Kategori",
                                    tint = EnuTheme.colors.contentBrandPrimaryDefault
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Kelola Kategori",
                                    style = EnuTheme.typography.ui.labels.normalCase.base,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                            Text(
                                text = "›",
                                style = EnuTheme.typography.ui.labels.normalCase.large,
                                color = EnuTheme.colors.contentDefaultSubtle
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "USERS",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentDefaultSubtle
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onKelolaUserClick),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_user),
                                    contentDescription = "Kelola User",
                                    tint = EnuTheme.colors.contentBrandPrimaryDefault
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Kelola User",
                                    style = EnuTheme.typography.ui.labels.normalCase.base,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                            Text(
                                text = "›",
                                style = EnuTheme.typography.ui.labels.normalCase.large,
                                color = EnuTheme.colors.contentDefaultSubtle
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "APPEARANCE",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentDefaultSubtle
                    )

                    EnuThemeSelector(
                        selectedMode = selectedTheme,
                        onModeSelected = onThemeSelected
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ABOUT",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentDefaultSubtle
                    )

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = "App Info",
                                    tint = EnuTheme.colors.contentBrandPrimaryDefault
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Enuventory",
                                    style = EnuTheme.typography.ui.labels.normalCase.base,
                                    color = EnuTheme.colors.contentDefaultPrimary
                                )
                            }
                            Text(
                                text = appVersion,
                                style = EnuTheme.typography.ui.labels.normalCase.base,
                                color = EnuTheme.colors.contentDefaultSubtle
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EnuTheme.colors.backgroundSignalErrorMediumDefault)
                        .clickable { showSignOutConfirmation = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Out",
                        style = EnuTheme.typography.ui.labels.normalCase.large,
                        color = EnuTheme.colors.contentSignalErrorDefault
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Preview
@Composable
fun SettingsAdminPagePreviewLight() {
    EnuTheme(darkTheme = false) {
        SettingsAdminPage(
            username = "Username",
            role = "Admin",
            appVersion = "v1.0.0",
            currentRoute = "settings",
            selectedTheme = AppThemeMode.Light,
            onThemeSelected = {},
            onBottomBarItemClick = {},
            onSignOutClick = {},
            onKelolaKategoriClick = {},
            onKelolaUserClick = {},
            isAdmin = true
        )
    }
}

@Preview
@Composable
fun SettingsAdminPagePreviewDark() {
    EnuTheme(darkTheme = true) {
        SettingsAdminPage(
            username = "Username",
            role = "Admin",
            appVersion = "v1.0.0",
            currentRoute = "settings",
            selectedTheme = AppThemeMode.Dark,
            onThemeSelected = {},
            onBottomBarItemClick = {},
            onSignOutClick = {},
            onKelolaKategoriClick = {},
            onKelolaUserClick = {},
            isAdmin = true
        )
    }
}
