package dev.stefano.enuventory.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun EnuTab(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = EnuTheme.colors.surfaceDefaultLevel3,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tabTitle ->
            val isSelected = index == selectedTabIndex

            val animatedTextColor by animateColorAsState(
                targetValue = if (isSelected) EnuTheme.colors.contentDefaultPrimary else EnuTheme.colors.contentDefaultSubtle,
                label = "TabTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isSelected) EnuTheme.colors.surfaceDefaultBase else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onTabSelected(index)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabTitle,
                    color = animatedTextColor,
                    style = EnuTheme.typography.ui.labels.normalCase.large
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuTabPreviewLight() {
    val tabItems = listOf("Aktif", "Selesai")
    var selectedTab1 by remember { mutableIntStateOf(0) }
    var selectedTab2 by remember { mutableIntStateOf(1) }

    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            EnuTab(
                tabs = tabItems,
                selectedTabIndex = selectedTab1,
                onTabSelected = { selectedTab1 = it }
            )

            Spacer(modifier = Modifier.height(24.dp))


            EnuTab(
                tabs = tabItems,
                selectedTabIndex = selectedTab2,
                onTabSelected = { selectedTab2 = it }
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuTabPreviewDark() {
    val tabItems = listOf("Aktif", "Selesai")
    var selectedTab1 by remember { mutableIntStateOf(0) }
    var selectedTab2 by remember { mutableIntStateOf(1) }

    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            EnuTab(
                tabs = tabItems,
                selectedTabIndex = selectedTab1,
                onTabSelected = { selectedTab1 = it }
            )

            Spacer(modifier = Modifier.height(24.dp))


            EnuTab(
                tabs = tabItems,
                selectedTabIndex = selectedTab2,
                onTabSelected = { selectedTab2 = it }
            )
        }
    }
}