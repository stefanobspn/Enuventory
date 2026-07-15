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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.stefano.enuventory.ui.theme.EnuTheme
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Dialog form pengajuan peminjaman: tanggal pinjam, tanggal kembali, dan alasan.
 * Tanggal dikirim sebagai epoch millis (awal hari, timezone device).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnuBorrowDialog(
    onDismissRequest: () -> Unit,
    onSubmitClick: (borrowDateMillis: Long, returnEstimateMillis: Long, alasan: String) -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false
) {
    var alasanInput by remember { mutableStateOf("") }
    var borrowDateMillis by remember { mutableStateOf<Long?>(null) }
    var returnDateMillis by remember { mutableStateOf<Long?>(null) }
    var showBorrowDatePicker by remember { mutableStateOf(false) }
    var showReturnDatePicker by remember { mutableStateOf(false) }

    if (showBorrowDatePicker) {
        BorrowDatePickerDialog(
            // Tanggal pinjam gak masuk akal kalau di masa lalu
            minUtcMillis = startOfTodayUtcMillis(),
            onDismiss = { showBorrowDatePicker = false },
            onConfirm = { millis ->
                borrowDateMillis = millis
                // Tanggal kembali gak boleh sebelum tanggal pinjam — reset kalau jadi invalid
                if (returnDateMillis?.let { it < millis } == true) returnDateMillis = null
                showBorrowDatePicker = false
            }
        )
    }

    if (showReturnDatePicker) {
        BorrowDatePickerDialog(
            minUtcMillis = borrowDateMillis ?: startOfTodayUtcMillis(),
            onDismiss = { showReturnDatePicker = false },
            onConfirm = { millis ->
                returnDateMillis = millis
                showReturnDatePicker = false
            }
        )
    }

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
                BorrowFieldLabel("Tanggal pinjam") {
                    EnuTextField(
                        value = borrowDateMillis?.let(::formatPickedDate).orEmpty(),
                        onValueChange = {},
                        placeholder = "Pilih tanggal",
                        readOnly = true,
                        onClick = { showBorrowDatePicker = true }
                    )
                }

                BorrowFieldLabel("Tanggal kembali") {
                    EnuTextField(
                        value = returnDateMillis?.let(::formatPickedDate).orEmpty(),
                        onValueChange = {},
                        placeholder = "Pilih tanggal",
                        readOnly = true,
                        onClick = { showReturnDatePicker = true }
                    )
                }

                BorrowFieldLabel("Alasan") {
                    EnuTextField(
                        value = alasanInput,
                        onValueChange = { alasanInput = it },
                        placeholder = "Tuliskan keperluanmu disini...",
                        singleLine = false,
                        minLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val isFormValid = borrowDateMillis != null &&
                        returnDateMillis != null &&
                        alasanInput.isNotBlank()
                val buttonVariant = when {
                    isSubmitting -> EnuButtonVariant.Loading
                    !isFormValid -> EnuButtonVariant.Disabled
                    else -> EnuButtonVariant.Normal
                }

                EnuButton(
                    text = "Submit",
                    variant = buttonVariant,
                    onClick = {
                        val borrow = borrowDateMillis ?: return@EnuButton
                        val estimate = returnDateMillis ?: return@EnuButton
                        if (alasanInput.isNotBlank()) {
                            onSubmitClick(borrow, estimate, alasanInput.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BorrowFieldLabel(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(
                text = label,
                style = EnuTheme.typography.ui.labels.normalCase.small,
                color = EnuTheme.colors.contentDefaultPrimary
            )
            Text(
                text = " *",
                style = EnuTheme.typography.ui.labels.normalCase.small,
                color = EnuTheme.colors.contentSignalErrorDefault
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BorrowDatePickerDialog(
    minUtcMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= minUtcMillis
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { datePickerState.selectedDateMillis?.let(onConfirm) ?: onDismiss() }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// DatePicker mengembalikan millis UTC di awal hari tanggal terpilih -- format eksplisit
// pakai timezone UTC di sini biar gak geser 1 hari tergantung timezone device.
private fun formatPickedDate(utcMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(java.util.Date(utcMillis))
}

private fun startOfTodayUtcMillis(): Long {
    val calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
