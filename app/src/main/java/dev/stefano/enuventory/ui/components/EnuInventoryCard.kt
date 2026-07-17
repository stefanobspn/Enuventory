package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.stefano.enuventory.ui.theme.EnuTheme
import dev.stefano.enuventory.ui.util.shimmerLoading

@Composable
fun EnuInventoryCard(
    title: String,
    id: String,
    status: EnuInventoryStatus,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = EnuTheme.colors.surfaceDefaultBase
        ),
        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
                            .size(width = 70.dp, height = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerLoading(true)
                    )
                } else {
                    Text(
                        text = title,
                        style = EnuTheme.typography.ui.labels.normalCase.large,
                        color = EnuTheme.colors.contentDefaultPrimary
                    )
                    Text(
                        text = "ID: $id",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentDefaultSubtle
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    EnuInventoryStatusBadge(status = status)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuInventoryCardPreviewLight() {
    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuInventoryCard(
                title = "Arduino Micro Controller",
                id = "HW-0019-A",
                status = EnuInventoryStatus.Tersedia,
                isLoading = false
            )

            EnuInventoryCard(
                title = "",
                id = "",
                status = EnuInventoryStatus.Tersedia,
                isLoading = true
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuInventoryCardPreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuInventoryCard(
                title = "Arduino Micro Controller",
                id = "HW-0019-A",
                status = EnuInventoryStatus.Tersedia,
                isLoading = false
            )

            EnuInventoryCard(
                title = "",
                id = "",
                status = EnuInventoryStatus.Tersedia,
                isLoading = true
            )
        }
    }
}