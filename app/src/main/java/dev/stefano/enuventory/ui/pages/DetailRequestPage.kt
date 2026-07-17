package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import dev.stefano.enuventory.ui.common.EnuEmptyState
import dev.stefano.enuventory.ui.common.EnuErrorState
import dev.stefano.enuventory.ui.common.UiState
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuDetailHistoryCard
import dev.stefano.enuventory.ui.components.EnuReasonDialog
import dev.stefano.enuventory.ui.components.EnuReturnProcessDialog
import dev.stefano.enuventory.ui.components.EnuScheduleDialog
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.formatDate
import dev.stefano.enuventory.ui.util.formatDateTime

/**
 * Halaman detail request untuk admin — aksinya mengikuti status record:
 * Pending → Approve (dialog jadwal) / Tolak (dialog alasan);
 * Borrowed → Proses Pengembalian (dialog kondisi Normal/Rusak);
 * status lainnya menampilkan info hasilnya.
 */
@Composable
fun DetailRequestPage(
    state: UiState<BorrowRecord>,
    assetImageUrl: String?,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onApproveConfirm: (pickupScheduleMillis: Long) -> Unit,
    onRejectConfirm: (rejectionReason: String) -> Unit,
    onReturnConfirm: (isDamaged: Boolean, damageNotes: String?) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }

    if (showScheduleDialog) {
        EnuScheduleDialog(
            onDismissRequest = { showScheduleDialog = false },
            onConfirmClick = { scheduleMillis ->
                showScheduleDialog = false
                onApproveConfirm(scheduleMillis)
            }
        )
    }

    if (showRejectDialog) {
        EnuReasonDialog(
            title = "Alasan Penolakan",
            placeholder = "Tuliskan alasan penolakan...",
            submitText = "Tolak",
            onDismissRequest = { showRejectDialog = false },
            onSubmitClick = { reason ->
                showRejectDialog = false
                onRejectConfirm(reason)
            }
        )
    }

    if (showReturnDialog) {
        EnuReturnProcessDialog(
            onDismissRequest = { showReturnDialog = false },
            onConfirmClick = { isDamaged, damageNotes ->
                showReturnDialog = false
                onReturnConfirm(isDamaged, damageNotes)
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Detail Request",
                showBack = true,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            EnuBottomBar(
                isAdmin = true,
                currentRoute = currentRoute,
                onItemClick = onBottomBarItemClick
            )
        },
        containerColor = EnuTheme.colors.surfaceDefaultBase
    ) { innerPadding ->
        when (state) {
            is UiState.Success -> {
                val record = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    EnuDetailHistoryCard(
                        title = record.assetTitle,
                        id = record.assetId,
                        imageUrl = assetImageUrl
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
                        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoField("Peminjam", record.borrowerName)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    InfoField("Tanggal pinjam", formatDate(record.borrowDate))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    InfoField("Estimasi Kembali", formatDate(record.returnEstimate))
                                }
                            }

                            InfoField("Alasan", record.reason)

                            // Info tambahan sesuai progres record
                            record.pickupSchedule?.let {
                                InfoField("Jadwal Pengambilan", formatDateTime(it))
                            }
                            record.pickedUpAt?.let {
                                InfoField("Diambil pada", formatDateTime(it))
                            }
                            record.returnDate?.let {
                                InfoField("Dikembalikan pada", formatDateTime(it))
                            }
                            record.rejectionReason?.let {
                                InfoField("Alasan Penolakan", it)
                            }
                            record.damageNotes?.let {
                                InfoField("Catatan Kerusakan", it)
                            }
                        }
                    }

                    when (record.status) {
                        BorrowStatus.Pending -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EnuButton(
                                    text = "Approve",
                                    variant = EnuButtonVariant.Normal,
                                    onClick = { showScheduleDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                EnuButton(
                                    text = "Tolak",
                                    variant = EnuButtonVariant.Danger,
                                    onClick = { showRejectDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        BorrowStatus.Borrowed -> {
                            EnuButton(
                                text = "Proses Pengembalian",
                                variant = EnuButtonVariant.Normal,
                                onClick = { showReturnDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        else -> {
                            val (statusText, statusColor) = record.statusInfo()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Status: $statusText",
                                    style = EnuTheme.typography.ui.labels.normalCase.large,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = EnuTheme.colors.contentBrandPrimaryDefault
                    )
                }
            }

            is UiState.Error -> {
                EnuErrorState(errorMessage = state.message, onRetryClick = onRetryClick)
            }

            is UiState.Empty -> {
                EnuEmptyState("Detail request tidak ditemukan")
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = EnuTheme.typography.content.headings.h6,
            color = EnuTheme.colors.contentDefaultSubtle
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = EnuTheme.typography.ui.labels.normalCase.base,
            color = EnuTheme.colors.contentDefaultPrimary
        )
    }
}

@Composable
private fun BorrowRecord.statusInfo(): Pair<String, Color> = when (status) {
    BorrowStatus.WaitingPickup -> "Menunggu Pengambilan" to EnuTheme.colors.contentSignalWarningDefault
    BorrowStatus.Rejected -> "Ditolak" to EnuTheme.colors.contentSignalErrorDefault
    BorrowStatus.Completed -> "Selesai" to EnuTheme.colors.contentSignalSuccessDefault
    BorrowStatus.Damaged -> "Dikembalikan Rusak" to EnuTheme.colors.contentSignalErrorDefault
    else -> "Pending" to EnuTheme.colors.contentSignalWarningDefault
}

@Preview(showBackground = true, name = "Light")
@Composable
fun DetailRequestPagePreviewLight() {
    val dummyRecord = BorrowRecord(
        id = "BR-001",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "U-001",
        borrowerName = "Budi",
        status = BorrowStatus.Pending,
        requestedAt = 1_792_195_200_000L,
        borrowDate = 1_792_195_200_000L,
        returnEstimate = 1_792_540_800_000L,
        reason = "Peminjaman alat untuk proyek kelas"
    )
    EnuTheme {
        DetailRequestPage(
            state = UiState.Success(dummyRecord),
            assetImageUrl = null,
            currentRoute = "approval",
            onBottomBarItemClick = {},
            onBackClick = {},
            onApproveConfirm = {},
            onRejectConfirm = {},
            onReturnConfirm = { _, _ -> },
            onRetryClick = {}
        )
    }
}
