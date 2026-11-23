package com.rvbhati.phototool.engine

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.sqrt

data class CompressedResult(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val qualityUsed: Int,
    val sizeKb: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompressedResult
        return bytes.contentEquals(other.bytes) &&
                width == other.width &&
                height == other.height &&
                qualityUsed == other.qualityUsed &&
                sizeKb == other.sizeKb
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + qualityUsed
        result = 31 * result + sizeKb.hashCode()
        return result
    }
}

object KbTargetCompressor {

    suspend fun compressToMaxKb(
        sourceBitmap: Bitmap,
        targetMaxKb: Int,
        minQuality: Int = 12,
        maxQuality: Int = 95
    ): CompressedResult = withContext(Dispatchers.Default) {
        val maxBytes = targetMaxKb * 1024
        var workingBitmap = sourceBitmap

        // Step 1: Binary search on JPEG quality at current resolution
        var bestResult = searchBestQuality(workingBitmap, maxBytes, minQuality, maxQuality)

        // Step 2: If still larger than maxBytes, progressively scale down dimensions
        var scaleFactor = 0.9f
        while (bestResult.bytes.size > maxBytes && scaleFactor >= 0.25f) {
            val newW = max(64, (sourceBitmap.width * scaleFactor).toInt())
            val newH = max(64, (sourceBitmap.height * scaleFactor).toInt())
            val scaled = Bitmap.createScaledBitmap(sourceBitmap, newW, newH, true)

            val candidate = searchBestQuality(scaled, maxBytes, minQuality, maxQuality)
            if (workingBitmap !== sourceBitmap && workingBitmap !== scaled) {
                workingBitmap.recycle()
            }
            workingBitmap = scaled
            bestResult = candidate

            // Jump scale faster if size is way above target
            val ratio = sqrt(maxBytes.toDouble() / max(1, candidate.bytes.size).toDouble()).toFloat()
            scaleFactor = minOf(scaleFactor - 0.1f, maxOf(0.25f, scaleFactor * ratio * 0.95f))
        }

        if (workingBitmap !== sourceBitmap) {
            workingBitmap.recycle()
        }

        bestResult.copy(sizeKb = bestResult.bytes.size / 1024.0)
    }

    private fun searchBestQuality(
        bitmap: Bitmap,
        maxBytes: Int,
        minQ: Int,
        maxQ: Int
    ): CompressedResult {
        var low = minQ
        var high = maxQ
        var bestBytes: ByteArray? = null
        var bestQuality = minQ

        while (low <= high) {
            val mid = (low + high) / 2
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, mid, out)
            val data = out.toByteArray()

            if (data.size <= maxBytes) {
                bestBytes = data
                bestQuality = mid
                low = mid + 1 // Try higher quality while staying <= maxBytes
            } else {
                high = mid - 1 // Lower quality to reduce KB size
            }
        }

        if (bestBytes != null) {
            return CompressedResult(
                bytes = bestBytes,
                width = bitmap.width,
                height = bitmap.height,
                qualityUsed = bestQuality,
                sizeKb = bestBytes.size / 1024.0
            )
        }

        // Fallback if even minQuality exceeds maxBytes
        val fallbackOut = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, minQ, fallbackOut)
        val fallbackData = fallbackOut.toByteArray()
        return CompressedResult(
            bytes = fallbackData,
            width = bitmap.width,
            height = bitmap.height,
            qualityUsed = minQ,
            sizeKb = fallbackData.size / 1024.0
        )
    }
}
