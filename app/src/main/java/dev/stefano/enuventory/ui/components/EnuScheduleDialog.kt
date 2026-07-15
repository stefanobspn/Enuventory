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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Dialog admin untuk menetapkan jadwal pengambilan barang saat menyetujui request:
 * pilih tanggal + jam, hasilnya dikirim sebagai epoch millis (timezone device).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnuScheduleDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (scheduleMillis: Long) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Jadwal Pengambilan",
    isSubmitting: Boolean = false
) {
    var dateUtcMillis by remember { mutableStateOf<Long?>(null) }
    var hour by remember { mutableStateOf<Int?>(null) }
    var minute by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        ScheduleDatePickerDialog(
            minUtcMillis = startOfTodayUtcMillis(),
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                dateUtcMillis = millis
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        ScheduleTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                hour = h
                minute = m
                showTimePicker = false
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
                Text(
                    text = title,
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )

                ScheduleFieldLabel("Tanggal") {
                    EnuTextField(
                        value = dateUtcMillis?.let(::formatScheduleDate).orEmpty(),
                        onValueChange = {},
                        placeholder = "Pilih tanggal",
                        readOnly = true,
                        onClick = { showDatePicker = true }
                    )
                }

                ScheduleFieldLabel("Jam") {
                    EnuTextField(
                        value = if (hour != null && minute != null) {
                            "${hour.toString().padStart(2, '0')}:${
                                minute.toString().padStart(2, '0')
                            }"
                        } else {
                            ""
                        },
                        onValueChange = {},
                        placeholder = "Pilih jam",
                        readOnly = true,
                        onClick = { showTimePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val isFormValid = dateUtcMillis != null && hour != null && minute != null
                val buttonVariant = when {
                    isSubmitting -> EnuButtonVariant.Loading
                    !isFormValid -> EnuButtonVariant.Disabled
                    else -> EnuButtonVariant.Normal
                }

                EnuButton(
                    text = "Konfirmasi",
                    variant = buttonVariant,
                    onClick = {
                        val date = dateUtcMillis ?: return@EnuButton
                        val h = hour ?: return@EnuButton
                        val m = minute ?: return@EnuButton
                        onConfirmClick(combineToLocalMillis(date, h, m))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScheduleFieldLabel(label: String, content: @Composable () -> Unit) {
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
private fun ScheduleDatePickerDialog(
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

// Material3 belum punya TimePickerDialog bawaan — TimePicker dibungkus Dialog sendiri.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = 9, is24Hour = true)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(EnuTheme.colors.surfaceDefaultBase)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimePicker(state = timePickerState)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
                TextButton(
                    onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
                ) {
                    Text("OK")
                }
            }
        }
    }
}

// DatePicker mengembalikan millis UTC awal hari — format eksplisit pakai UTC
// biar gak geser 1 hari tergantung timezone device.
private fun formatScheduleDate(utcMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(java.util.Date(utcMillis))
}

private fun startOfTodayUtcMillis(): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

// Tanggal dari DatePicker itu UTC, tapi jam yang dipilih admin maksudnya jam lokal —
// gabungkan komponen tanggal (dibaca via UTC) dengan jam:menit di timezone device.
private fun combineToLocalMillis(dateUtcMillis: Long, hour: Int, minute: Int): Long {
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = dateUtcMillis
    }
    return Calendar.getInstance().apply {
        set(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH),
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuScheduleDialogPreviewLight() {
    EnuTheme {
        EnuScheduleDialog(
            onDismissRequest = {},
            onConfirmClick = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
fun EnuScheduleDialogPreviewDark() {
    EnuTheme(darkTheme = true) {
        EnuScheduleDialog(
            onDismissRequest = {},
            onConfirmClick = {}
        )
    }
}
