package com.rvbhati.phototool.engine

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ColorFilterParams(
    val brightness: Float = 0f,   // -100 to +100
    val contrast: Float = 1f,      // 0.2 to 2.5
    val saturation: Float = 1f,    // 0.0 to 2.5
    val warmth: Float = 0f         // -50 to +50 (blue to yellow/red)
)

object ColorAdjustEngine {

    suspend fun applyColorFilters(
        source: Bitmap,
        params: ColorFilterParams
    ): Bitmap = withContext(Dispatchers.Default) {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Combine matrices:
        // 1. Brightness & Contrast matrix
        val scale = params.contrast.coerceIn(0.1f, 3.0f)
        val translate = params.brightness.coerceIn(-100f, 100f)

        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // 2. Saturation matrix
        val satMatrix = ColorMatrix().apply {
            setSaturation(params.saturation.coerceIn(0f, 3f))
        }

        // 3. Warmth matrix (boost red/lower blue for warm, vice versa for cool)
        val warmthOffset = params.warmth.coerceIn(-50f, 50f)
        val warmthMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, warmthOffset * 0.8f,
                0f, 1f, 0f, 0f, warmthOffset * 0.3f,
                0f, 0f, 1f, 0f, -warmthOffset * 0.8f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Combine all matrices
        val combined = ColorMatrix()
        combined.postConcat(contrastMatrix)
        combined.postConcat(satMatrix)
        combined.postConcat(warmthMatrix)

        paint.colorFilter = ColorMatrixColorFilter(combined)
        canvas.drawBitmap(source, 0f, 0f, paint)

        output
    }
}
