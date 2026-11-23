package com.rvbhati.phototool.engine

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

enum class OutputFormat {
    JPG,
    PNG,
    WEBP
}

object ImageTransformEngine {

    suspend fun cropBitmap(source: Bitmap, cropRect: Rect): Bitmap = withContext(Dispatchers.Default) {
        val safeLeft = cropRect.left.coerceIn(0, source.width - 1)
        val safeTop = cropRect.top.coerceIn(0, source.height - 1)
        val safeWidth = cropRect.width().coerceIn(1, source.width - safeLeft)
        val safeHeight = cropRect.height().coerceIn(1, source.height - safeTop)

        Bitmap.createBitmap(source, safeLeft, safeTop, safeWidth, safeHeight)
    }

    suspend fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap = withContext(Dispatchers.Default) {
        val matrix = Matrix().apply { postRotate(degrees) }
        Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    suspend fun flipBitmap(source: Bitmap, horizontal: Boolean): Bitmap = withContext(Dispatchers.Default) {
        val matrix = Matrix().apply {
            if (horizontal) {
                postScale(-1f, 1f, source.width / 2f, source.height / 2f)
            } else {
                postScale(1f, -1f, source.width / 2f, source.height / 2f)
            }
        }
        Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    suspend fun resizeBitmap(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        keepAspectRatio: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        val safeW: Int
        val safeH: Int

        if (keepAspectRatio) {
            val ratio = source.width.toFloat() / source.height.toFloat()
            if (targetWidth > 0 && targetHeight > 0) {
                if (targetWidth / ratio <= targetHeight) {
                    safeW = targetWidth
                    safeH = max(1, (targetWidth / ratio).toInt())
                } else {
                    safeH = targetHeight
                    safeW = max(1, (targetHeight * ratio).toInt())
                }
            } else if (targetWidth > 0) {
                safeW = targetWidth
                safeH = max(1, (targetWidth / ratio).toInt())
            } else {
                safeH = targetHeight
                safeW = max(1, (targetHeight * ratio).toInt())
            }
        } else {
            safeW = max(1, targetWidth)
            safeH = max(1, targetHeight)
        }

        Bitmap.createScaledBitmap(source, safeW, safeH, true)
    }

    suspend fun convertFormat(
        source: Bitmap,
        format: OutputFormat,
        quality: Int = 90
    ): ByteArray = withContext(Dispatchers.Default) {
        val stream = ByteArrayOutputStream()
        when (format) {
            OutputFormat.JPG -> source.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(10, 100), stream)
            OutputFormat.PNG -> source.compress(Bitmap.CompressFormat.PNG, 100, stream)
            OutputFormat.WEBP -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    source.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality.coerceIn(10, 100), stream)
                } else {
                    @Suppress("DEPRECATION")
                    source.compress(Bitmap.CompressFormat.WEBP, quality.coerceIn(10, 100), stream)
                }
            }
        }
        stream.toByteArray()
    }
}
