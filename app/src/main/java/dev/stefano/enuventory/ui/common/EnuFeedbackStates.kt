package dev.stefano.enuventory.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.theme.EnuTheme

/**
 * Shared composable untuk state Error.
 * Menggantikan copy-paste blok error yang identik di setiap page.
 */
@Composable
fun EnuErrorState(
    errorMessage: String = "Terjadi Kesalahan",
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = "Error",
            tint = EnuTheme.colors.contentSignalErrorDefault,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Terjadi Kesalahan",
            style = EnuTheme.typography.ui.labels.normalCase.large,
            color = EnuTheme.colors.contentDefaultPrimary
        )
        Text(
            text = errorMessage,
            style = EnuTheme.typography.ui.labels.normalCase.small,
            color = EnuTheme.colors.contentSignalErrorDefault,
            modifier = Modifier.padding(vertical = 4.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        EnuButton(
            text = "Coba lagi",
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

/**
 * Shared composable untuk state Empty.
 * Menggantikan copy-paste blok empty yang identik di setiap page.
 */
@Composable
fun EnuEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_info),
            contentDescription = "Empty",
            tint = EnuTheme.colors.contentBrandPrimaryDefault,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = EnuTheme.typography.ui.labels.normalCase.base,
            color = EnuTheme.colors.contentDefaultPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Error - Light")
@Composable
private fun EnuErrorStatePreviewLight() {
    EnuTheme {
        EnuErrorState(
            errorMessage = "Tidak dapat terhubung ke server",
            onRetryClick = {}
        )
    }
}

@Preview(name = "Error - Dark")
@Composable
private fun EnuErrorStatePreviewDark() {
    EnuTheme(darkTheme = true) {
        EnuErrorState(
            errorMessage = "Tidak dapat terhubung ke server",
            onRetryClick = {}
        )
    }
}

@Preview(name = "Empty - Light")
@Composable
private fun EnuEmptyStatePreviewLight() {
    EnuTheme {
        EnuEmptyState(message = "Belum ada data")
    }
}

@Preview(name = "Empty - Dark")
@Composable
private fun EnuEmptyStatePreviewDark() {
    EnuTheme(darkTheme = true) {
        EnuEmptyState(message = "Belum ada data")
    }
}
