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

enum class EnuBorrowStatus {
    Dipinjam, Menunggu, MenungguPengambilan, Ditolak, Selesai, Rusak, Terlambat
}

@Composable
fun EnuBorrowStatusBadge(
    status: EnuBorrowStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val contentColor: Color
    val labelText: String

    when (status) {
        EnuBorrowStatus.Dipinjam -> {
            backgroundColor = EnuTheme.colors.backgroundSignalSuccessMediumDefault
            contentColor = EnuTheme.colors.contentSignalSuccessOnSubtle
            labelText = "DIPINJAM"
        }

        EnuBorrowStatus.Menunggu -> {
            backgroundColor = EnuTheme.colors.backgroundSignalWarningMediumDefault
            contentColor = EnuTheme.colors.contentSignalWarningOnSubtle
            labelText = "MENUNGGU"
        }

        EnuBorrowStatus.MenungguPengambilan -> {
            backgroundColor = EnuTheme.colors.backgroundSignalWarningMediumDefault
            contentColor = EnuTheme.colors.contentSignalWarningOnSubtle
            labelText = "MENUNGGU PENGAMBILAN"
        }

        EnuBorrowStatus.Ditolak -> {
            backgroundColor = EnuTheme.colors.backgroundSignalErrorMediumDefault
            contentColor = EnuTheme.colors.contentSignalErrorOnSubtle
            labelText = "DITOLAK"
        }

        EnuBorrowStatus.Selesai -> {
            backgroundColor = EnuTheme.colors.backgroundSignalSuccessMediumDefault
            contentColor = EnuTheme.colors.contentSignalSuccessOnSubtle
            labelText = "SELESAI"
        }

        EnuBorrowStatus.Rusak -> {
            backgroundColor = EnuTheme.colors.backgroundSignalErrorMediumDefault
            contentColor = EnuTheme.colors.contentSignalErrorOnSubtle
            labelText = "RUSAK"
        }

        EnuBorrowStatus.Terlambat -> {
            backgroundColor = EnuTheme.colors.backgroundSignalErrorMediumDefault
            contentColor = EnuTheme.colors.contentSignalErrorOnSubtle
            labelText = "TERLAMBAT"
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
            style = EnuTheme.typography.ui.labels.allCaps.small
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuBorrowStatusBadgePreviewLight() {
    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Dipinjam)
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Menunggu)
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Ditolak)
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Selesai)
        }
    }
}

@Preview(name = "Dark Mode")
@Composable
fun EnuBorrowStatusBadgePreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Dipinjam)
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Menunggu)
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Ditolak)
            EnuBorrowStatusBadge(status = EnuBorrowStatus.Selesai)
        }
    }
}