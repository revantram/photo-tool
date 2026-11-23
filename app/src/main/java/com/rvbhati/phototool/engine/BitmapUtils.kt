package com.rvbhati.phototool.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import kotlin.math.max

object BitmapUtils {

    suspend fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int = 2048,
        reqHeight: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext null

            // Handle EXIF orientation
            val orientation = getExifOrientation(context, uri)
            rotateBitmap(sampledBitmap, orientation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return source
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotated !== source) {
            source.recycle()
        }
        return rotated
    }

    fun createTempImageFile(context: Context, extension: String = "jpg"): File {
        val dir = File(context.cacheDir, "temp_images").apply { mkdirs() }
        return File(dir, "photo_tool_${System.currentTimeMillis()}.$extension")
    }

    fun clearCache(context: Context): Long {
        var bytesFreed = 0L
        val cacheDirs = listOf(
            context.cacheDir,
            File(context.cacheDir, "temp_images"),
            File(context.cacheDir, "pdf_extracted")
        )
        for (dir in cacheDirs) {
            if (dir.exists()) {
                dir.listFiles()?.forEach { file ->
                    bytesFreed += file.length()
                    file.delete()
                }
            }
        }
        return bytesFreed
    }
}
