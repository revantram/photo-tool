package com.rvbhati.phototool.engine

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WatermarkOptions(
    val text: String = "SAMPLE",
    val textColor: Int = Color.WHITE,
    val textSize: Float = 48f,
    val opacity: Float = 0.5f,
    val angle: Float = -30f,
    val tileMode: Boolean = true
)

object WatermarkEngine {

    suspend fun addTextWatermark(
        source: Bitmap,
        options: WatermarkOptions
    ): Bitmap = withContext(Dispatchers.Default) {
        val output = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = options.textColor
            alpha = (options.opacity.coerceIn(0.05f, 1f) * 255).toInt()
            textSize = options.textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        if (options.tileMode) {
            val stepX = (paint.measureText(options.text) * 1.8f).toInt().coerceAtLeast(120)
            val stepY = (options.textSize * 4f).toInt().coerceAtLeast(100)

            for (x in -output.width until output.width * 2 step stepX) {
                for (y in -output.height until output.height * 2 step stepY) {
                    canvas.save()
                    canvas.rotate(options.angle, x.toFloat(), y.toFloat())
                    canvas.drawText(options.text, x.toFloat(), y.toFloat(), paint)
                    canvas.restore()
                }
            }
        } else {
            // Single center watermark
            val centerX = output.width / 2f
            val centerY = output.height / 2f
            canvas.save()
            canvas.rotate(options.angle, centerX, centerY)
            canvas.drawText(options.text, centerX, centerY, paint)
            canvas.restore()
        }

        output
    }
}
