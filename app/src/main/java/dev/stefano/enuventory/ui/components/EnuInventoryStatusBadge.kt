package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.ui.theme.EnuTheme

enum class EnuInventoryStatus {
    Tersedia, Direservasi, Maintenance
}

@Composable
fun EnuInventoryStatusBadge(
    status: EnuInventoryStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val contentColor: Color
    val labelText: String

    when (status) {
        EnuInventoryStatus.Tersedia -> {
            backgroundColor = EnuTheme.colors.backgroundSignalSuccessMediumDefault
            contentColor = EnuTheme.colors.contentSignalSuccessOnSubtle
            labelText = "Tersedia"
        }

        EnuInventoryStatus.Direservasi -> {
            backgroundColor = EnuTheme.colors.backgroundSignalErrorMediumDefault
            contentColor = EnuTheme.colors.contentSignalErrorOnSubtle
            labelText = "Direservasi"
        }

        EnuInventoryStatus.Maintenance -> {
            backgroundColor = EnuTheme.colors.backgroundSignalWarningMediumDefault
            contentColor = EnuTheme.colors.contentSignalWarningOnSubtle
            labelText = "Maintenance"
        }
    }

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = labelText,
            color = contentColor,
            style = EnuTheme.typography.ui.labels.normalCase.small
        )
    }
}

@Preview(name = "Light Mode")
@Composable
fun EnuInventoryStatusBadgePreviewLight() {
    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuInventoryStatusBadge(status = EnuInventoryStatus.Tersedia)
            EnuInventoryStatusBadge(status = EnuInventoryStatus.Direservasi)
            EnuInventoryStatusBadge(status = EnuInventoryStatus.Maintenance)
        }
    }
}

@Preview(name = "Dark Mode")
@Composable
fun EnuInventoryStatusBadgePreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuInventoryStatusBadge(status = EnuInventoryStatus.Tersedia)
            EnuInventoryStatusBadge(status = EnuInventoryStatus.Direservasi)
            EnuInventoryStatusBadge(status = EnuInventoryStatus.Maintenance)
        }
    }
}