package com.rvbhati.phototool.engine

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

enum class PrivacyMode {
    PIXELATE,
    BLUR,
    BLACKOUT
}

object PrivacyBrushEngine {

    suspend fun applyPrivacyToRect(
        source: Bitmap,
        rect: Rect,
        mode: PrivacyMode,
        blockSize: Int = 20
    ): Bitmap = withContext(Dispatchers.Default) {
        val safeLeft = rect.left.coerceIn(0, source.width - 1)
        val safeTop = rect.top.coerceIn(0, source.height - 1)
        val safeRight = rect.right.coerceIn(safeLeft + 1, source.width)
        val safeBottom = rect.bottom.coerceIn(safeTop + 1, source.height)
        val width = safeRight - safeLeft
        val height = safeBottom - safeTop

        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        when (mode) {
            PrivacyMode.BLACKOUT -> {
                val paint = Paint().apply {
                    color = Color.BLACK
                    style = Paint.Style.FILL
                }
                canvas.drawRect(RectF(safeLeft.toFloat(), safeTop.toFloat(), safeRight.toFloat(), safeBottom.toFloat()), paint)
            }
            PrivacyMode.PIXELATE -> {
                val targetBlock = max(4, blockSize)
                val smallW = max(1, width / targetBlock)
                val smallH = max(1, height / targetBlock)

                val subBmp = Bitmap.createBitmap(source, safeLeft, safeTop, width, height)
                val downscaled = Bitmap.createScaledBitmap(subBmp, smallW, smallH, false)
                val upscaled = Bitmap.createScaledBitmap(downscaled, width, height, false)

                canvas.drawBitmap(upscaled, safeLeft.toFloat(), safeTop.toFloat(), null)
                subBmp.recycle()
                downscaled.recycle()
                upscaled.recycle()
            }
            PrivacyMode.BLUR -> {
                // Downsample & upsample with linear filter for fast offline box blur
                val blurScale = 8
                val smallW = max(1, width / blurScale)
                val smallH = max(1, height / blurScale)

                val subBmp = Bitmap.createBitmap(source, safeLeft, safeTop, width, height)
                val downscaled = Bitmap.createScaledBitmap(subBmp, smallW, smallH, true)
                val upscaled = Bitmap.createScaledBitmap(downscaled, width, height, true)

                canvas.drawBitmap(upscaled, safeLeft.toFloat(), safeTop.toFloat(), null)
                subBmp.recycle()
                downscaled.recycle()
                upscaled.recycle()
            }
        }

        result
    }
}
