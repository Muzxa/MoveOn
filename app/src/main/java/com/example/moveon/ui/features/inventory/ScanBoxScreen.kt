package com.example.moveon.ui.features.inventory

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScanBoxScreen(
    onBack: () -> Unit,
    onScanned: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasCameraPermission = remember { mutableStateOf(false) }
    val scannedValue = remember { mutableStateOf<String?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission.value = isGranted
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission.value = true
        }
    }

    LaunchedEffect(scannedValue.value) {
        scannedValue.value?.let {
            onScanned(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        if (hasCameraPermission.value) {
            CameraPreviewBox(
                onQrScanned = { value ->
                    scannedValue.value = value
                },
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor
            )
        }

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Scanner overlay frame (persistent on top)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // QR Frame Container
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Semi-transparent overlay background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // Orange corner brackets (top-left)
                QrFrameCorner(
                    modifier = Modifier.align(Alignment.TopStart),
                    top = true,
                    left = true
                )

                // Orange corner brackets (top-right)
                QrFrameCorner(
                    modifier = Modifier.align(Alignment.TopEnd),
                    top = true,
                    left = false
                )

                // Orange corner brackets (bottom-left)
                QrFrameCorner(
                    modifier = Modifier.align(Alignment.BottomStart),
                    top = false,
                    left = true
                )

                // Orange corner brackets (bottom-right)
                QrFrameCorner(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    top = false,
                    left = false
                )

                // Horizontal scan line with gradient
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x00FF6F00),
                                    Color(0x99FF6F00),
                                    Color(0x00FF6F00)
                                )
                            )
                        )
                )

                // White center scanning area
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(189.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                )
            }

            // Instruction text
            Text(
                text = "Align QR code within frame",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun CameraPreviewBox(
    onQrScanned: (String) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        val previewView = previewViewRef.value ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = BarcodeScanning.getClient(options)

                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImage(
                                imageProxy,
                                scanner,
                                onQrScanned
                            )
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            previewViewRef.value = previewView
            previewView
        }
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrScanned: (String) -> Unit
) {
    try {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            onQrScanned(rawValue)
                            return@addOnSuccessListener
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    } catch (e: Exception) {
        imageProxy.close()
        e.printStackTrace()
    }
}

@Composable
private fun QrFrameCorner(
    modifier: Modifier,
    top: Boolean,
    left: Boolean
) {
    Box(modifier = modifier.size(50.dp)) {
        val cornerColor = Color(0xCCFF6F00)
        val lineWidth = 3.dp
        val lineLength = 20.dp

        if (top && left) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(width = lineLength, height = lineWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(width = lineWidth, height = lineLength)
                    .background(cornerColor)
            )
        } else if (top && !left) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = lineLength, height = lineWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = lineWidth, height = lineLength)
                    .background(cornerColor)
            )
        } else if (!top && left) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(width = lineLength, height = lineWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(width = lineWidth, height = lineLength)
                    .background(cornerColor)
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = lineLength, height = lineWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = lineWidth, height = lineLength)
                    .background(cornerColor)
            )
        }
    }
}
