package com.example.moveon.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type

@Suppress("DEPRECATION")
class YuvToRgbConverter(context: Context) {
    private val renderScript = RenderScript.create(context)
    private val yuvToRgbScript = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript))

    private var yuvBuffer: ByteArray? = null
    private var inputAllocation: Allocation? = null
    private var outputAllocation: Allocation? = null

    fun yuvToRgb(image: Image, output: Bitmap) {
        val width = image.width
        val height = image.height
        val requiredSize = width * height * 3 / 2

        val buffer = yuvBuffer?.takeIf { it.size == requiredSize } ?: ByteArray(requiredSize).also {
            yuvBuffer = it
        }

        yuv420ToNv21(image, buffer)

        val inputType = Type.Builder(renderScript, Element.U8(renderScript)).setX(buffer.size)
        if (inputAllocation == null || inputAllocation?.type?.x != buffer.size) {
            inputAllocation = Allocation.createTyped(renderScript, inputType.create(), Allocation.USAGE_SCRIPT)
        }

        if (outputAllocation == null || outputAllocation?.type?.x != output.width || outputAllocation?.type?.y != output.height) {
            outputAllocation = Allocation.createFromBitmap(renderScript, output)
        }

        inputAllocation?.copyFrom(buffer)
        yuvToRgbScript.setInput(inputAllocation)
        yuvToRgbScript.forEach(outputAllocation)
        outputAllocation?.copyTo(output)
    }

    private fun yuv420ToNv21(image: Image, output: ByteArray) {
        val width = image.width
        val height = image.height
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        var outputOffset = 0

        for (row in 0 until height) {
            val yRowStart = row * yRowStride
            for (col in 0 until width) {
                output[outputOffset++] = yBuffer.get(yRowStart + col)
            }
        }

        val chromaHeight = height / 2
        val chromaWidth = width / 2

        for (row in 0 until chromaHeight) {
            val uRowStart = row * uvRowStride
            val vRowStart = row * uvRowStride
            for (col in 0 until chromaWidth) {
                val uvOffset = col * uvPixelStride
                output[outputOffset++] = vBuffer.get(vRowStart + uvOffset)
                output[outputOffset++] = uBuffer.get(uRowStart + uvOffset)
            }
        }
    }
}
