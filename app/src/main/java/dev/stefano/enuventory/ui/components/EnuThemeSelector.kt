package dev.stefano.enuventory.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.domain.model.AppThemeMode
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun EnuThemeSelector(
    selectedMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = EnuTheme.colors.backgroundNeutralMediumDefault,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AppThemeMode.entries.forEach { mode ->
            val isSelected = selectedMode == mode
            val isEnabled = mode != AppThemeMode.Dark

            val iconRes = when (mode) {
                AppThemeMode.Light -> R.drawable.ic_sun
                AppThemeMode.Dark -> R.drawable.ic_moon
            }

            val contentColor = when {
                !isEnabled -> EnuTheme.colors.contentDefaultSubtle.copy(alpha = 0.4f)
                isSelected -> EnuTheme.colors.contentBrandPrimaryDefault
                else -> EnuTheme.colors.contentDefaultSubtle
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isSelected) EnuTheme.colors.surfaceDefaultBase else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = isEnabled
                    ) { onModeSelected(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = mode.name,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mode.name,
                        color = contentColor,
                        style = EnuTheme.typography.ui.labels.normalCase.small
                    )
                }
            }
        }
    }
}