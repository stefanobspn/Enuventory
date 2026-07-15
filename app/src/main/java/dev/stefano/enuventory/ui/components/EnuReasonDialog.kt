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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.ui.theme.EnuTheme

/**
 * Dialog generik untuk meminta teks alasan/catatan dari admin — dipakai untuk
 * alasan penolakan request dan catatan kerusakan barang.
 */
@Composable
fun EnuReasonDialog(
    title: String,
    placeholder: String,
    onDismissRequest: () -> Unit,
    onSubmitClick: (reason: String) -> Unit,
    modifier: Modifier = Modifier,
    submitText: String = "Submit",
    isSubmitting: Boolean = false
) {
    var reasonInput by remember { mutableStateOf("") }

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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = title,
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )

                EnuTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    placeholder = placeholder,
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { if (!isSubmitting) onDismissRequest() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Batal",
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    EnuButton(
                        text = submitText,
                        variant = when {
                            isSubmitting -> EnuButtonVariant.Loading
                            reasonInput.isBlank() -> EnuButtonVariant.Disabled
                            else -> EnuButtonVariant.Normal
                        },
                        onClick = {
                            if (reasonInput.isNotBlank()) onSubmitClick(reasonInput.trim())
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuReasonDialogPreviewLight() {
    EnuTheme {
        EnuReasonDialog(
            title = "Alasan Penolakan",
            placeholder = "Tuliskan alasan penolakan...",
            onDismissRequest = {},
            onSubmitClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun EnuReasonDialogPreviewDark() {
    EnuTheme(darkTheme = true) {
        EnuReasonDialog(
            title = "Alasan Penolakan",
            placeholder = "Tuliskan alasan penolakan...",
            onDismissRequest = {},
            onSubmitClick = {}
        )
    }
}
