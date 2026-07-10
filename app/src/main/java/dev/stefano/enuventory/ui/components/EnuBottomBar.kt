package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.theme.EnuTheme


data class EnuBottomBarItemData(
    val route: String,
    val label: String,
    val icon: Int
)

@Composable
fun EnuBottomBar(
    isAdmin: Boolean,
    currentRoute: String?,
    onItemClick: (EnuBottomBarItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = remember(isAdmin) {
        if (isAdmin) EnuBottomBarDefaults.AdminMenus else EnuBottomBarDefaults.UserMenus
    }

    Surface(
        color = EnuTheme.colors.surfaceDefaultLevel3,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars), // padding buat nav bar system
        ) {
            navigationItems.forEach { item ->
                val isSelected = currentRoute == item.route

                EnuBottomBarNavItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

object EnuBottomBarDefaults {
    private val HomeItem =
        EnuBottomBarItemData(route = "home", label = "Home", icon = R.drawable.ic_home)
    private val HistoryItem =
        EnuBottomBarItemData(route = "history", label = "History", icon = R.drawable.ic_history)
    private val SettingsItem =
        EnuBottomBarItemData(route = "settings", label = "Settings", icon = R.drawable.ic_settings)
    private val ApprovalItem =
        EnuBottomBarItemData(route = "approval", label = "Approval", icon = R.drawable.ic_approval)

    val UserMenus = listOf(HomeItem, HistoryItem, SettingsItem)
    val AdminMenus = listOf(HomeItem, ApprovalItem, SettingsItem)
}

@Preview
@Composable
fun EnuBottomBarPreviewLight() {
    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            EnuBottomBar(
                isAdmin = false,
                currentRoute = "home",
                onItemClick = {}
            )

            Spacer(Modifier.height(8.dp))

            EnuBottomBar(
                isAdmin = true,
                currentRoute = "home",
                onItemClick = {}
            )
        }
    }
}

@Preview
@Composable
fun EnuBottomBarPreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            EnuBottomBar(
                isAdmin = false,
                currentRoute = "home",
                onItemClick = {}
            )

            Spacer(Modifier.height(8.dp))

            EnuBottomBar(
                isAdmin = true,
                currentRoute = "home",
                onItemClick = {}
            )
        }
    }
}

