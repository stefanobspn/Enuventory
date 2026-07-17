package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.ui.theme.EnuTheme

/**
 * Dialog konfirmasi generik buat aksi yang butuh double-check dari user sebelum diproses
 * (mis. Sign Out, Hapus Asset) -- supaya aksi destruktif gak langsung jalan begitu tombolnya
 * ke-tap, terutama kalau ke-tap gak sengaja.
 */
@Composable
fun EnuConfirmationDialog(
    title: String,
    message: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Ya",
    cancelText: String = "Batal",
    isDanger: Boolean = true
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = EnuTheme.typography.ui.labels.normalCase.base,
                    color = EnuTheme.colors.contentDefaultSubtle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EnuTheme.colors.backgroundNeutralMediumDefault)
                            .clickable(onClick = onDismissRequest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cancelText,
                            style = EnuTheme.typography.ui.labels.normalCase.large,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                    }

                    EnuButton(
                        text = confirmText,
                        onClick = onConfirmClick,
                        variant = if (isDanger) EnuButtonVariant.Danger else EnuButtonVariant.Normal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(name = "Light")
@Composable
private fun EnuConfirmationDialogPreviewLight() {
    EnuTheme {
        EnuConfirmationDialog(
            title = "Sign Out",
            message = "Kamu yakin ingin keluar dari akun ini?",
            onConfirmClick = {},
            onDismissRequest = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
private fun EnuConfirmationDialogPreviewDark() {
    EnuTheme(darkTheme = true) {
        EnuConfirmationDialog(
            title = "Hapus Asset",
            message = "Asset yang sudah dihapus tidak bisa dikembalikan.",
            onConfirmClick = {},
            onDismissRequest = {}
        )
    }
}
