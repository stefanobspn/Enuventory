package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun EnuDetailHistoryCard(
    title: String,
    id: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = EnuTheme.colors.surfaceDefaultBase
        ),
        border = BorderStroke(1.dp, EnuTheme.colors.borderDefaultMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF5D5353))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = EnuTheme.typography.ui.labels.normalCase.large,
                    color = EnuTheme.colors.contentDefaultPrimary
                )
                Text(
                    text = "ID: $id",
                    style = EnuTheme.typography.content.headings.h6,
                    color = EnuTheme.colors.contentDefaultSubtle
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuDetailHistoryCardPreviewLight() {
    EnuTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EnuDetailHistoryCard(
                title = "Arduino Micro Controller",
                id = "HW-0019-A"
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuDetailHistoryCardPreviewDark() {
    EnuTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            EnuDetailHistoryCard(
                title = "Arduino Micro Controller",
                id = "HW-0019-A"
            )
        }
    }
}

