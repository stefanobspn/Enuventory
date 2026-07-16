package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.shimmerLoading
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun EnuHistoryCard(
    title: String,
    id: String,
    status: EnuBorrowStatus,
    borrowDate: String,
    returnEstimate: String,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFinished: Boolean = false,
    isLoading: Boolean = false,
    imageUrl: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EnuTheme.colors.surfaceDefaultBase
        ),
        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerLoading(isLoading)
                        .background(
                            if (isLoading) Color.Transparent
                            else Color.White
                        )
                ) {
                    if (!isLoading && imageUrl != null) {
                        coil3.compose.AsyncImage(
                            model = imageUrl,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerLoading(true)
                        )
                    } else {
                        Text(
                            text = title,
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                        Text(
                            text = "ID: $id",
                            style = EnuTheme.typography.ui.labels.normalCase.small,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                        Spacer(Modifier.height(4.dp))

                        EnuBorrowStatusBadge(status = status)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                    } else {
                        Text(
                            text = "Tanggal pinjam",
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = borrowDate,
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerLoading(true)
                        )
                    } else {
                        Text(
                            text = if (isFinished) "Tanggal Kembali" else "Estimasi Kembali",
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = returnEstimate,
                            style = EnuTheme.typography.ui.labels.normalCase.base,
                            color = EnuTheme.colors.contentDefaultPrimary
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerLoading(true)
                )
            } else {
                EnuButton(
                    text = "Lihat Detail",
                    onClick = onDetailClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuHistoryCardPreviewLight() {
    EnuTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuHistoryCard(
                title = "Arduino Micro Controller",
                id = "HW-0019-A",
                status = EnuBorrowStatus.Dipinjam,
                borrowDate = "16 Okt 26",
                returnEstimate = "16 Okt 26",
                onDetailClick = {}
            )

            EnuHistoryCard(
                title = "", id = "",
                status = EnuBorrowStatus.Dipinjam,
                borrowDate = "", returnEstimate = "",
                onDetailClick = {},
                isLoading = true
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuHistoryCardPreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuHistoryCard(
                title = "Arduino Micro Controller",
                id = "HW-0019-A",
                status = EnuBorrowStatus.Dipinjam,
                borrowDate = "16 Okt 26",
                returnEstimate = "16 Okt 26",
                onDetailClick = {}
            )

            EnuHistoryCard(
                title = "", id = "",
                status = EnuBorrowStatus.Dipinjam,
                borrowDate = "", returnEstimate = "",
                onDetailClick = {},
                isLoading = true
            )
        }
    }
}