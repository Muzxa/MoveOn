package com.example.moveon.ui.features.inventory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.moveon.ui.components.ObjectDetectionOverlayView
import com.example.moveon.ui.utils.YuvToRgbConverter
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.support.image.TensorImage
import java.util.concurrent.atomic.AtomicBoolean

class ObjectDetectionAnalyzer(
    context: Context,
    private val detector: ObjectDetector,
    private val overlayView: ObjectDetectionOverlayView,
    private val frameSkip: Int = 2
) : ImageAnalysis.Analyzer {

    private val yuvToRgbConverter = YuvToRgbConverter(context)
    private var bitmapBuffer: Bitmap? = null
    private var rotatedBuffer: Bitmap? = null
    private var frameCounter: Int = 0
    private val isProcessing = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (frameCounter++ % (frameSkip + 1) != 0) {
            imageProxy.close()
            return
        }

        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        try {
            val image = imageProxy.image ?: return
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = bitmapBuffer
                ?.takeIf { it.width == imageProxy.width && it.height == imageProxy.height }
                ?: Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                ).also { bitmapBuffer = it }

            yuvToRgbConverter.yuvToRgb(image, bitmap)

            val rotatedBitmap = if (rotationDegrees == 0) {
                bitmap
            } else {
                val targetWidth = if (rotationDegrees % 180 == 0) bitmap.width else bitmap.height
                val targetHeight = if (rotationDegrees % 180 == 0) bitmap.height else bitmap.width
                val buffer = rotatedBuffer
                    ?.takeIf { it.width == targetWidth && it.height == targetHeight }
                    ?: Bitmap.createBitmap(
                        targetWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888
                    ).also { rotatedBuffer = it }

                buffer.eraseColor(Color.TRANSPARENT)

                val matrix = Matrix()
                when (rotationDegrees) {
                    90 -> {
                        matrix.postRotate(90f)
                        matrix.postTranslate(bitmap.height.toFloat(), 0f)
                    }

                    180 -> {
                        matrix.postRotate(180f)
                        matrix.postTranslate(bitmap.width.toFloat(), bitmap.height.toFloat())
                    }

                    270 -> {
                        matrix.postRotate(270f)
                        matrix.postTranslate(0f, bitmap.width.toFloat())
                    }

                    else -> {
                        matrix.postRotate(rotationDegrees.toFloat())
                    }
                }

                val canvas = Canvas(buffer)
                canvas.drawBitmap(bitmap, matrix, null)
                buffer
            }

            val tensorImage = TensorImage.fromBitmap(rotatedBitmap)
            val results = detector.detect(tensorImage)
            val imageWidth = rotatedBitmap.width
            val imageHeight = rotatedBitmap.height

            overlayView.setResults(results, imageWidth, imageHeight)

            if (results.isNotEmpty()) {
                val summary = results.joinToString { detection ->
                    val category = detection.categories.maxByOrNull { it.score }
                    val label = category?.label ?: "unknown"
                    val score = category?.score ?: 0f
                    "$label ${(score * 100).toInt()}%"
                }
                Log.d(TAG, "Detections: $summary")
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Object detection failed", exception)
        } finally {
            isProcessing.set(false)
            imageProxy.close()
        }
    }

    companion object {
        private const val TAG = "ObjectDetection"
    }
}
