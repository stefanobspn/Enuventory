package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.theme.EnuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnuTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showNotification: Boolean = false,
    notificationCount: Int = 0,
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = EnuTheme.typography.content.headings.h3,
                color = EnuTheme.colors.contentDefaultPrimary
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = EnuTheme.colors.contentDefaultPrimary
                    )
                }
            }
        },
        actions = {
            if (showNotification) {
                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge {
                                    Text(if (notificationCount > 99) "99+" else notificationCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification),
                            contentDescription = "Notification",
                            tint = EnuTheme.colors.contentDefaultPrimary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = EnuTheme.colors.surfaceDefaultLevel3,
            navigationIconContentColor = EnuTheme.colors.contentDefaultPrimary,
            titleContentColor = EnuTheme.colors.contentDefaultPrimary,
            actionIconContentColor = EnuTheme.colors.contentDefaultPrimary
        ),
        windowInsets = WindowInsets.statusBars // handle padding system status bar
    )
}

@Preview(name = "Light")
@Composable
fun EnuTopBarPreviewLight() {
    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuTopBar(
                title = "Page",
                showNotification = true
            )

            EnuTopBar(
                title = "Page",
                showNotification = true,
                notificationCount = 3
            )

            EnuTopBar(
                title = "Page"
            )

            EnuTopBar(
                title = "Page",
                showBack = true
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuTopBarPreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuTopBar(
                title = "Page",
                showNotification = true
            )

            EnuTopBar(
                title = "Page",
                showNotification = true,
                notificationCount = 3
            )

            EnuTopBar(
                title = "Page"
            )

            EnuTopBar(
                title = "Page",
                showBack = true
            )
        }
    }
}