package com.rvbhati.phototool.engine

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

data class JoinedPhotoSignatureResult(
    val bitmap: Bitmap,
    val jpegBytes: ByteArray,
    val sizeKb: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as JoinedPhotoSignatureResult
        return bitmap == other.bitmap &&
                jpegBytes.contentEquals(other.jpegBytes) &&
                sizeKb == other.sizeKb
    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + jpegBytes.contentHashCode()
        result = 31 * result + sizeKb.hashCode()
        return result
    }
}

object PhotoSignatureJoinerEngine {

    suspend fun joinPhotoAndSignature(
        photoBitmap: Bitmap,
        signatureBitmap: Bitmap,
        outputWidth: Int = 200,
        outputHeight: Int = 250,
        photoRatio: Float = 0.72f,
        gapPx: Int = 6,
        backgroundColor: Int = Color.WHITE,
        jpegQuality: Int = 90
    ): JoinedPhotoSignatureResult = withContext(Dispatchers.Default) {
        val safeW = outputWidth.coerceAtLeast(64)
        val safeH = outputHeight.coerceAtLeast(64)
        val safeGap = gapPx.coerceIn(0, safeH / 5)

        val resultBitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(backgroundColor)

        val clampedRatio = photoRatio.coerceIn(0.45f, 0.85f)
        val availableHeight = (safeH - safeGap).coerceAtLeast(20)
        val photoBoxHeight = (availableHeight * clampedRatio).toInt().coerceAtLeast(10)
        val signBoxHeight = (availableHeight - photoBoxHeight).coerceAtLeast(10)

        // 1. Draw Photo centered inside top box (preserving aspect ratio)
        drawFittedBitmap(
            canvas = canvas,
            source = photoBitmap,
            boxLeft = 0f,
            boxTop = 0f,
            boxWidth = safeW.toFloat(),
            boxHeight = photoBoxHeight.toFloat()
        )

        // 2. Draw Signature centered inside bottom box (preserving aspect ratio)
        val signTop = (photoBoxHeight + safeGap).toFloat()
        drawFittedBitmap(
            canvas = canvas,
            source = signatureBitmap,
            boxLeft = 0f,
            boxTop = signTop,
            boxWidth = safeW.toFloat(),
            boxHeight = signBoxHeight.toFloat()
        )

        val outStream = ByteArrayOutputStream()
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality.coerceIn(10, 100), outStream)
        val bytes = outStream.toByteArray()

        JoinedPhotoSignatureResult(
            bitmap = resultBitmap,
            jpegBytes = bytes,
            sizeKb = bytes.size / 1024.0
        )
    }

    private fun drawFittedBitmap(
        canvas: Canvas,
        source: Bitmap,
        boxLeft: Float,
        boxTop: Float,
        boxWidth: Float,
        boxHeight: Float
    ) {
        if (source.width <= 0 || source.height <= 0) return

        val scale = min(boxWidth / source.width.toFloat(), boxHeight / source.height.toFloat())
        val drawW = (source.width * scale).coerceAtLeast(1f)
        val drawH = (source.height * scale).coerceAtLeast(1f)
        val dx = boxLeft + (boxWidth - drawW) / 2f
        val dy = boxTop + (boxHeight - drawH) / 2f

        val destRect = RectF(dx, dy, dx + drawW, dy + drawH)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, null, destRect, paint)
    }
}
