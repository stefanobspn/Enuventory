package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.ui.theme.EnuTheme

/**
 * Dialog admin untuk memproses pengembalian barang: pilih kondisi Normal atau Rusak.
 * Kalau Rusak, catatan kerusakan wajib diisi — status barang otomatis jadi Maintenance.
 */
@Composable
fun EnuReturnProcessDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (isDamaged: Boolean, damageNotes: String?) -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false
) {
    var isDamaged by remember { mutableStateOf(false) }
    var damageNotesInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismissRequest() }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EnuTheme.colors.surfaceDefaultBase)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Proses Pengembalian",
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )

                Text(
                    text = "Periksa kondisi barang lalu pilih hasilnya:",
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentDefaultSubtle
                )

                ConditionOption(
                    label = "Normal — barang kembali tersedia",
                    selected = !isDamaged,
                    onClick = { isDamaged = false }
                )
                ConditionOption(
                    label = "Rusak — barang masuk maintenance",
                    selected = isDamaged,
                    onClick = { isDamaged = true }
                )

                if (isDamaged) {
                    EnuTextField(
                        value = damageNotesInput,
                        onValueChange = { damageNotesInput = it },
                        placeholder = "Tuliskan catatan kerusakan...",
                        singleLine = false,
                        minLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val isFormValid = !isDamaged || damageNotesInput.isNotBlank()

                EnuButton(
                    text = "Konfirmasi",
                    variant = when {
                        isSubmitting -> EnuButtonVariant.Loading
                        !isFormValid -> EnuButtonVariant.Disabled
                        else -> EnuButtonVariant.Normal
                    },
                    onClick = {
                        if (isFormValid) {
                            onConfirmClick(
                                isDamaged,
                                damageNotesInput.trim().takeIf { isDamaged }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ConditionOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = EnuTheme.typography.ui.labels.normalCase.base,
            color = EnuTheme.colors.contentDefaultPrimary
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuReturnProcessDialogPreviewLight() {
    EnuTheme {
        EnuReturnProcessDialog(
            onDismissRequest = {},
            onConfirmClick = { _, _ -> }
        )
    }
}

@Preview(name = "Dark")
@Composable
fun EnuReturnProcessDialogPreviewDark() {
    EnuTheme(darkTheme = true) {
        EnuReturnProcessDialog(
            onDismissRequest = {},
            onConfirmClick = { _, _ -> }
        )
    }
}
