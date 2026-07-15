package dev.stefano.enuventory.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import dev.stefano.enuventory.ui.components.EnuDetailHistoryCard
import dev.stefano.enuventory.ui.components.EnuTimeline
import dev.stefano.enuventory.ui.components.EnuTimelineItemData
import dev.stefano.enuventory.ui.components.EnuTimelineNodeStatus
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.screen.history.DetailRiwayatUiModel
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.formatDate
import dev.stefano.enuventory.ui.util.formatDateTime

enum class DetailRiwayatState {
    MenungguPersetujuan, MenungguPengambilan, BatasKembali, Dikembalikan, DikembalikanRusak, Ditolak
}

@Composable
fun DetailRiwayatPage(
    state: UiState<DetailRiwayatUiModel>,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Detail Riwayat",
                showBack = true,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            EnuBottomBar(
                isAdmin = false,
                currentRoute = currentRoute,
                onItemClick = onBottomBarItemClick
            )
        },
        containerColor = EnuTheme.colors.surfaceDefaultBase
    ) { innerPadding ->
        when (state) {
            is UiState.Success -> {
                val data = state.data
                val record = data.record
                val riwayatState = data.riwayatState

                val nowMillis = remember { System.currentTimeMillis() }
                val timelineItems = remember(riwayatState, record) {
                    when (riwayatState) {
                        DetailRiwayatState.MenungguPersetujuan -> listOf(
                            EnuTimelineItemData(
                                "Diajukan",
                                formatDate(record.requestedAt),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData("Menunggu Persetujuan", null, EnuTimelineNodeStatus.Current),
                            EnuTimelineItemData("Diambil", null, EnuTimelineNodeStatus.Upcoming),
                            EnuTimelineItemData("Batas Kembali", null, EnuTimelineNodeStatus.Upcoming)
                        )

                        DetailRiwayatState.MenungguPengambilan -> listOf(
                            EnuTimelineItemData(
                                "Diajukan",
                                formatDate(record.requestedAt),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Disetujui",
                                formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Menunggu Pengambilan",
                                record.pickupSchedule?.let { "Jadwal: ${formatDateTime(it)}" },
                                EnuTimelineNodeStatus.Current
                            ),
                            EnuTimelineItemData("Batas Kembali", null, EnuTimelineNodeStatus.Upcoming)
                        )

                        DetailRiwayatState.BatasKembali -> listOf(
                            EnuTimelineItemData(
                                "Diajukan",
                                formatDate(record.requestedAt),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Disetujui",
                                formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Diambil",
                                record.pickedUpAt?.let(::formatDate)
                                    ?: formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Batas Kembali",
                                if (record.isOverdue(nowMillis)) {
                                    "${formatDate(record.returnEstimate)} (Terlambat)"
                                } else {
                                    formatDate(record.returnEstimate)
                                },
                                if (record.isOverdue(nowMillis)) {
                                    EnuTimelineNodeStatus.Rejected
                                } else {
                                    EnuTimelineNodeStatus.Current
                                }
                            )
                        )

                        DetailRiwayatState.Dikembalikan -> listOf(
                            EnuTimelineItemData(
                                "Diajukan",
                                formatDate(record.requestedAt),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Disetujui",
                                formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Diambil",
                                record.pickedUpAt?.let(::formatDate)
                                    ?: formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Dikembalikan",
                                "Selesai",
                                EnuTimelineNodeStatus.Completed
                            )
                        )

                        DetailRiwayatState.DikembalikanRusak -> listOf(
                            EnuTimelineItemData(
                                "Diajukan",
                                formatDate(record.requestedAt),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Disetujui",
                                formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Diambil",
                                record.pickedUpAt?.let(::formatDate)
                                    ?: formatDate(record.borrowDate),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Dikembalikan (Rusak)",
                                record.damageNotes,
                                EnuTimelineNodeStatus.Rejected
                            )
                        )

                        DetailRiwayatState.Ditolak -> listOf(
                            EnuTimelineItemData(
                                "Diajukan",
                                formatDate(record.requestedAt),
                                EnuTimelineNodeStatus.Completed
                            ),
                            EnuTimelineItemData(
                                "Ditolak",
                                record.rejectionReason,
                                EnuTimelineNodeStatus.Rejected
                            )
                        )
                    }
                }

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
                        imageUrl = data.assetImageUrl
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = EnuTheme.colors.surfaceDefaultBase),
                        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Aktivitas Pinjaman",
                                style = EnuTheme.typography.ui.labels.normalCase.large,
                                color = EnuTheme.colors.contentDefaultPrimary
                            )

                            EnuTimeline(items = timelineItems)
                        }
                    }

                    when (riwayatState) {
                        DetailRiwayatState.MenungguPengambilan -> {
                            EnuButton(
                                text = "Scan QR",
                                onClick = onScanQrClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        DetailRiwayatState.BatasKembali -> {
                            // Pengembalian diproses oleh admin — user cukup datang ke kantor.
                            Text(
                                text = "Kembalikan barang ke kantor — pengembalian akan diproses oleh admin.",
                                style = EnuTheme.typography.ui.labels.normalCase.base,
                                color = EnuTheme.colors.contentDefaultSubtle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        else -> {}
                    }
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
                EnuEmptyState("Riwayat tidak ditemukan")
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun DetailRiwayatPagePreviewLight() {
    val dummyRecord = BorrowRecord(
        id = "BR-001",
        assetId = "HW-0019-A",
        assetTitle = "Arduino Micro Controller",
        borrowerId = "U-001",
        borrowerName = "Budi",
        status = BorrowStatus.Borrowed,
        requestedAt = 1_792_108_800_000L,
        borrowDate = 1_792_108_800_000L,
        returnEstimate = 1_792_540_800_000L,
        reason = "Peminjaman alat untuk proyek kelas"
    )
    EnuTheme {
        DetailRiwayatPage(
            state = UiState.Success(DetailRiwayatUiModel(dummyRecord, DetailRiwayatState.BatasKembali)),
            currentRoute = "history",
            onBottomBarItemClick = {},
            onBackClick = {},
            onScanQrClick = {},
            onRetryClick = {}
        )
    }
}