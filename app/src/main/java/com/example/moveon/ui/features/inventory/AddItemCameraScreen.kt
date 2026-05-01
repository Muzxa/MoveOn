package com.example.moveon.ui.features.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import java.io.File

@Composable
fun AddItemCameraScreen(
    boxUuid: String,
    boxId: String,
    onBack: () -> Unit,
    onItemSaved: () -> Unit,
    viewModel: BoxItemsViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val hasCameraPermission = remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedImageUri by remember { mutableStateOf<String?>(null) }
    var isCapturingImage by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission.value = granted
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            hasCameraPermission.value = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BoxItemsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission.value) {
            AddItemCameraPreview(
                lifecycleOwner = lifecycleOwner,
                onImageCaptureReady = { imageCapture = it }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera permission is required",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x33101828), Color(0x661E2939))
                    )
                )
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        MoveOnPillButton(
            text = "Manually Log Item",
            onClick = {
                val capture = imageCapture
                if (capture == null) {
                    Toast.makeText(context, "Camera is not ready yet", Toast.LENGTH_SHORT).show()
                    return@MoveOnPillButton
                }
                if (isCapturingImage) return@MoveOnPillButton

                isCapturingImage = true
                val imageFile = File(context.cacheDir, "item_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            capturedImageUri = outputFileResults.savedUri?.toString()
                                ?: Uri.fromFile(imageFile).toString()
                            showLogDialog = true
                            isCapturingImage = false
                        }

                        override fun onError(exception: ImageCaptureException) {
                            isCapturingImage = false
                            Toast.makeText(
                                context,
                                "Failed to capture image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .fillMaxWidth(),
            background = Primary
        )
    }

    if (showLogDialog) {
        AddItemDialog(
            imageUri = capturedImageUri,
            onDismiss = { showLogDialog = false },
            onSave = { name, quantity, description, isFragile, imageUrl ->
                viewModel.addItem(
                    boxId = boxId,
                    name = name,
                    quantity = quantity,
                    description = description,
                    isFragile = isFragile,
                    imageUrl = imageUrl,
                    onResult = { success ->
                        if (success) {
                            onItemSaved()
                        }
                    }
                )
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun AddItemCameraPreview(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        val previewView = previewViewRef.value ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                onImageCaptureReady(imageCapture)
            }
        }, mainExecutor)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).also { previewViewRef.value = it }
        }
    )
}

@Composable
fun AddItemDialog(
    imageUri: String?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        quantity: Int,
        description: String,
        isFragile: Boolean,
        imageUrl: String
    ) -> Unit,
    initialName: String = "",
    initialQuantity: Int = 1,
    initialDescription: String = "",
    initialIsFragile: Boolean = false,
    title: String = "Add Item",
    confirmLabel: String = "Save Changes"
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var quantityText by remember(initialQuantity) {
        mutableStateOf(initialQuantity.coerceAtLeast(1).toString())
    }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var isFragile by remember(initialIsFragile) { mutableStateOf(initialIsFragile) }
    val modalTextColor = Color.Black
    val modalHintColor = Color(0xFF424242)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = modalTextColor
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = LightTextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Captured item image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Item Name", color = modalTextColor) },
                                modifier = Modifier.weight(3f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = modalTextColor,
                                    unfocusedTextColor = modalTextColor,
                                    focusedLabelColor = modalTextColor,
                                    unfocusedLabelColor = modalTextColor,
                                    cursorColor = modalTextColor
                                )
                            )
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter(Char::isDigit) },
                                label = { Text("Quantity", color = modalTextColor) },
                                modifier = Modifier.weight(2f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = modalTextColor,
                                    unfocusedTextColor = modalTextColor,
                                    focusedLabelColor = modalTextColor,
                                    unfocusedLabelColor = modalTextColor,
                                    cursorColor = modalTextColor
                                )
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description", color = modalTextColor) },
                            placeholder = { Text("Enter item description", color = modalHintColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = modalTextColor,
                                unfocusedTextColor = modalTextColor,
                                focusedLabelColor = modalTextColor,
                                unfocusedLabelColor = modalTextColor,
                                focusedPlaceholderColor = modalHintColor,
                                unfocusedPlaceholderColor = modalHintColor,
                                cursorColor = modalTextColor
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Fragile Item",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = modalTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Requires special handling",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = modalHintColor
                                )
                            }
                            Switch(
                                checked = isFragile,
                                onCheckedChange = { isFragile = it }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoveOnPillButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        background = LightSurface,
                        textColor = LightTextPrimary
                    )
                    MoveOnPillButton(
                        text = confirmLabel,
                        onClick = {
                            val quantity = quantityText.toIntOrNull() ?: 1
                            onSave(name, quantity, description, isFragile, imageUri.orEmpty())
                        },
                        modifier = Modifier.weight(1f),
                        background = Primary
                    )
                }
            }
        }
    }
}
