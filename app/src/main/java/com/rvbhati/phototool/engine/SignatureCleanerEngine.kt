package com.rvbhati.phototool.engine

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

object SignatureCleanerEngine {

    /**
     * @param source Safed kagaz par signature ki photo
     * @param transparentBackground true = Transparent PNG, false = Pure White background (JPG/PNG)
     * @param inkThreshold 0.0..1.0 (jitna zyada, utni zyada halka ink bhi dark hoga)
     */
    suspend fun cleanSignature(
        source: Bitmap,
        transparentBackground: Boolean = true,
        inkThreshold: Float = 0.65f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val totalPixels = width * height
        val srcPixels = IntArray(totalPixels)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val outPixels = IntArray(totalPixels)
        val cutoffLuminance = (255f * inkThreshold.coerceIn(0.2f, 0.9f))

        for (i in 0 until totalPixels) {
            val pixel = srcPixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Perceived luminance (0 = dark ink, 255 = bright paper/glare)
            val luminance = 0.299f * r + 0.587f * g + 0.114f * b

            if (luminance >= cutoffLuminance) {
                // Paper background -> Transparent or Pure White
                outPixels[i] = if (transparentBackground) {
                    Color.TRANSPARENT
                } else {
                    Color.WHITE
                }
            } else {
                // Signature ink -> Darken smoothly for crisp contrast
                val darknessFactor = ((cutoffLuminance - luminance) / max(1f, cutoffLuminance))
                    .coerceIn(0f, 1f)
                val inkChannel = ((1f - min(1f, darknessFactor * 1.4f)) * 45f)
                    .toInt()
                    .coerceIn(0, 45)

                val alpha = if (transparentBackground) {
                    (120 + darknessFactor * 135f).toInt().coerceIn(0, 255)
                } else {
                    255
                }

                outPixels[i] = Color.argb(alpha, inkChannel, inkChannel, inkChannel)
            }
        }

        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(outPixels, 0, width, 0, 0, width, height)
        }
    }
}
