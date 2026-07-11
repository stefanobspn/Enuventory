package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.shimmerLoading

enum class EnuCategoryBadgeState {
    Selected, Unselected, Loading
}

@Composable
fun EnuCategoryBadge(
    text: String,
    state: EnuCategoryBadgeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val contentColor: Color

    when (state) {
        EnuCategoryBadgeState.Selected -> {
            backgroundColor = EnuTheme.colors.surfaceBrandPrimaryStrong
            contentColor = EnuTheme.colors.contentBrandPrimaryOnStrong
        }

        EnuCategoryBadgeState.Unselected -> {
            backgroundColor = EnuTheme.colors.backgroundNeutralMediumDefault
            contentColor = EnuTheme.colors.contentDefaultSubtle
        }

        EnuCategoryBadgeState.Loading -> {
            backgroundColor = EnuTheme.colors.backgroundNeutralMediumDefault
            contentColor = Color.Transparent
        }
    }

    Box(
        modifier = modifier
            .widthIn(min = 96.dp)
            .clip(CircleShape)
            .background(color = backgroundColor, shape = CircleShape)
            .shimmerLoading(state == EnuCategoryBadgeState.Loading)
            .clickable(
                enabled = state != EnuCategoryBadgeState.Loading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (state == EnuCategoryBadgeState.Loading) "Badge" else text,
            color = if (state == EnuCategoryBadgeState.Loading) Color.Transparent else contentColor,
            style = EnuTheme.typography.ui.labels.normalCase.base
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuCategoryBadgePreviewLight() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    EnuTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuCategoryBadge(
                text = "Badge",
                state = if (selectedIndex == 0) EnuCategoryBadgeState.Selected else EnuCategoryBadgeState.Unselected,
                onClick = { selectedIndex = 0 }
            )

            EnuCategoryBadge(
                text = "Badge",
                state = if (selectedIndex == 1) EnuCategoryBadgeState.Selected else EnuCategoryBadgeState.Unselected,
                onClick = { selectedIndex = 1 }
            )

            EnuCategoryBadge(
                text = "",
                state = EnuCategoryBadgeState.Loading,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuCategoryBadgePreviewDark() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuCategoryBadge(
                text = "Badge",
                state = if (selectedIndex == 0) EnuCategoryBadgeState.Selected else EnuCategoryBadgeState.Unselected,
                onClick = { selectedIndex = 0 }
            )

            EnuCategoryBadge(
                text = "Badge",
                state = if (selectedIndex == 1) EnuCategoryBadgeState.Selected else EnuCategoryBadgeState.Unselected,
                onClick = { selectedIndex = 1 }
            )

            EnuCategoryBadge(
                text = "",
                state = EnuCategoryBadgeState.Loading,
                onClick = {}
            )
        }
    }
}