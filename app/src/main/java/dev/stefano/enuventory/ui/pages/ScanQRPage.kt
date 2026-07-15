package dev.stefano.enuventory.ui.pages

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.components.EnuBottomBarItemData
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuConfirmationDialog
import dev.stefano.enuventory.ui.components.EnuTopBar
import dev.stefano.enuventory.ui.theme.EnuTheme
import java.util.concurrent.Executors
import androidx.camera.core.Preview as CameraPreview

enum class ScanQRUiState {
    Scanning, Confirming, Mismatch, Submitting, Error
}

/**
 * Konfirmasi pengambilan barang lewat scan QR asli (CameraX + ML Kit): hasil scan
 * dicocokkan dengan assetId record yang sedang MenungguPengambilan di [ScanQRViewModel].
 */
@Composable
fun ScanQRPage(
    state: ScanQRUiState,
    assetTitle: String,
    errorMessage: String?,
    currentRoute: String?,
    onBottomBarItemClick: (EnuBottomBarItemData) -> Unit,
    onBackClick: () -> Unit,
    onQrDetected: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onCancelConfirm: () -> Unit,
    onUlangiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (state == ScanQRUiState.Confirming) {
        EnuConfirmationDialog(
            title = "Konfirmasi Pengambilan",
            message = "\"$assetTitle\" akan ditandai sebagai Dipinjam.",
            onConfirmClick = onConfirmClick,
            onDismissRequest = onCancelConfirm,
            isDanger = false
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EnuTopBar(
                title = "Scan QR",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission && !LocalInspectionMode.current) {
                    QrCameraPreview(
                        onQrDetected = onQrDetected,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (!hasCameraPermission) {
                    CameraPermissionRequest(
                        onRequestClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(12.dp))
                    )

                    if (state == ScanQRUiState.Submitting) {
                        CircularProgressIndicator(color = Color.White)
                    }

                    if (state == ScanQRUiState.Mismatch) {
                        MismatchOverlay()
                    }

                    if (state == ScanQRUiState.Error) {
                        ErrorOverlay(message = errorMessage ?: "Gagal konfirmasi pengambilan")
                    }
                }
            }

            if (state == ScanQRUiState.Mismatch || state == ScanQRUiState.Error) {
                EnuButton(
                    text = "Ulangi",
                    onClick = onUlangiClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequest(onRequestClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Izin kamera dibutuhkan untuk scan QR",
            style = EnuTheme.typography.ui.labels.normalCase.base,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        EnuButton(text = "Berikan Izin", onClick = onRequestClick)
    }
}

@Composable
private fun MismatchOverlay() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(EnuTheme.colors.backgroundSignalErrorMediumDefault)
            .padding(12.dp)
    ) {
        Text(
            text = "QR tidak sesuai dengan asset ini",
            style = EnuTheme.typography.ui.labels.normalCase.small,
            color = EnuTheme.colors.contentSignalErrorOnSubtle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorOverlay(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = null,
            tint = EnuTheme.colors.contentSignalErrorDefault,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = message,
            style = EnuTheme.typography.ui.labels.normalCase.small,
            color = EnuTheme.colors.contentSignalErrorDefault,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Preview kamera + analisis QR real-time. Deteksi berulang dibatasi di ViewModel
 * ([onQrDetected] boleh dipanggil terus tanpa efek samping selagi bukan state Scanning).
 */
@Composable
private fun QrCameraPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = CameraPreview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(scanner, imageProxy, onQrDetected)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (_: Exception) {
                // Device tanpa kamera cocok -- user tetap bisa "Ulangi" dari state Error/Mismatch.
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProviderFuture.get().unbindAll()
            cameraExecutor.shutdown()
            scanner.close()
        }
    }
}

@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: androidx.camera.core.ImageProxy,
    onQrDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstNotNullOfOrNull { it.rawValue }?.let(onQrDetected)
        }
        .addOnCompleteListener { imageProxy.close() }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun ScanQRPagePreviewLight() {
    EnuTheme {
        ScanQRPage(
            state = ScanQRUiState.Scanning,
            assetTitle = "Arduino Micro Controller",
            errorMessage = null,
            currentRoute = "history",
            onBottomBarItemClick = {},
            onBackClick = {},
            onQrDetected = {},
            onConfirmClick = {},
            onCancelConfirm = {},
            onUlangiClick = {}
        )
    }
}

@Preview(name = "Dark - Error")
@Composable
fun ScanQRPagePreviewDark() {
    EnuTheme(darkTheme = true) {
        ScanQRPage(
            state = ScanQRUiState.Error,
            assetTitle = "Arduino Micro Controller",
            errorMessage = "Gagal konfirmasi pengambilan",
            currentRoute = "history",
            onBottomBarItemClick = {},
            onBackClick = {},
            onQrDetected = {},
            onConfirmClick = {},
            onCancelConfirm = {},
            onUlangiClick = {}
        )
    }
}
