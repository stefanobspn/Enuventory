package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun RowScope.EnuBottomBarNavItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = EnuTheme.colors.contentBrandPrimaryDefault,
    unselectedColor: Color = EnuTheme.colors.contentDefaultPrimary
) {
    val contentColor = if (isSelected) selectedColor else unselectedColor

    Column(
        modifier = modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = contentColor
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            style = EnuTheme.typography.ui.labels.normalCase.base,
            color = contentColor
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun EnuBottomBarNavItemPreviewLight() {
    EnuTheme {
        Row {
            EnuBottomBarNavItem(
                icon = R.drawable.ic_home,
                label = "Home",
                isSelected = true,
                onClick = { }
            )

            EnuBottomBarNavItem(
                icon = R.drawable.ic_home,
                label = "Home",
                isSelected = false,
                onClick = { }
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    name = "Dark Mode"
)
@Composable
fun EnuBottomBarNavItemPreviewDark() {
    EnuTheme(darkTheme = true) {
        Row {
            EnuBottomBarNavItem(
                icon = R.drawable.ic_home,
                label = "Home",
                isSelected = true,
                onClick = { }
            )

            EnuBottomBarNavItem(
                icon = R.drawable.ic_home,
                label = "Home",
                isSelected = false,
                onClick = { }
            )
        }
    }
}

