package dev.stefano.enuventory.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dev.stefano.enuventory.domain.util.AssetQrContent
import dev.stefano.enuventory.ui.theme.EnuTheme

/**
 * Render QR code untuk sebuah asset memakai zxing — kontennya di-encode lewat
 * [AssetQrContent] (bukan plain assetId) dan ditempel admin ke barang fisik, lalu
 * di-scan user saat pengambilan ([dev.stefano.enuventory.ui.pages.ScanQRPage]).
 */
@Composable
fun EnuQrCode(
    assetId: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 200
) {
    val content = remember(assetId) { AssetQrContent.encode(assetId) }
    val bitmap = remember(content, sizeDp) { generateQrBitmap(content, sizeDp) }

    Image(
        painter = BitmapPainter(bitmap.asImageBitmap()),
        contentDescription = "QR Code untuk $assetId",
        modifier = modifier
            .size(sizeDp.dp)
            .background(Color.White)
            .padding(8.dp)
    )
}

private fun generateQrBitmap(content: String, sizePx: Int): Bitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(
                x,
                y,
                if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuQrCodePreviewLight() {
    EnuTheme {
        EnuQrCode(assetId = "HW-0019-A")
    }
}
