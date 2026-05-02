package com.example.moveon.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import org.tensorflow.lite.task.vision.detector.Detection
import kotlin.math.max

class ObjectDetectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class DetectionBox(
        val boundingBox: RectF,
        val label: String,
        val score: Float,
        val index: Int
    )

    private val blueBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#1565C0")  // MoveOn Blue
        isAntiAlias = true
    }

    private val orangeBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#FF6F00")  // MoveOn Orange
        isAntiAlias = true
    }

    private val labelTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val labelBackgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#B3000000")
    }

    private var detectionBoxes: List<DetectionBox> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    fun setResults(
        results: List<Detection>,
        imageWidth: Int,
        imageHeight: Int
    ) {
        post {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            detectionBoxes = results.mapIndexed { index, detection ->
                val category = detection.categories.maxByOrNull { it.score } ?: return@mapIndexed null
                DetectionBox(
                    boundingBox = detection.boundingBox,
                    label = category.label,
                    score = category.score,
                    index = index
                )
            }.filterNotNull()
            invalidate()
        }
    }

    fun clear() {
        post {
            detectionBoxes = emptyList()
            invalidate()
        }
    }

    fun getDetectedItemCount(): Int = detectionBoxes.size

    fun getDetectedItems(): List<Pair<String, RectF>> = detectionBoxes.map { it.label to it.boundingBox }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0 || detectionBoxes.isEmpty()) {
            return
        }

        val scale = max(
            width / imageWidth.toFloat(),
            height / imageHeight.toFloat()
        )
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale
        val dx = (width - scaledWidth) / 2f
        val dy = (height - scaledHeight) / 2f

        detectionBoxes.forEach { detection ->
            val box = RectF(
                detection.boundingBox.left * scale + dx,
                detection.boundingBox.top * scale + dy,
                detection.boundingBox.right * scale + dx,
                detection.boundingBox.bottom * scale + dy
            )

            // Alternate between blue and orange
            val paint = if (detection.index % 2 == 0) blueBoxPaint else orangeBoxPaint
            canvas.drawRect(box, paint)

            val label = "${detection.label} ${(detection.score * 100).toInt()}%"
            val textWidth = labelTextPaint.measureText(label)
            val textHeight = labelTextPaint.textSize
            val textPadding = 8f

            val textLeft = box.left
            val textTop = max(0f, box.top - textHeight - textPadding * 2)
            val textBackground = RectF(
                textLeft,
                textTop,
                textLeft + textWidth + textPadding * 2,
                textTop + textHeight + textPadding * 2
            )

            canvas.drawRoundRect(textBackground, 8f, 8f, labelBackgroundPaint)
            canvas.drawText(
                label,
                textLeft + textPadding,
                textTop + textHeight + textPadding / 2,
                labelTextPaint
            )
        }
    }
}
