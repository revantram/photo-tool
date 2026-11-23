package com.rvbhati.phototool.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class ExifInfo(
    val cameraMake: String?,
    val cameraModel: String?,
    val dateTaken: String?,
    val iso: String?,
    val fNumber: String?,
    val exposureTime: String?,
    val focalLength: String?,
    val hasGps: Boolean
)

object ExifInspectorEngine {

    suspend fun readExif(context: Context, uri: Uri): ExifInfo = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                val exif = ExifInterface(stream)
                val latLong = exif.latLong
                ExifInfo(
                    cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE),
                    cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL),
                    dateTaken = exif.getAttribute(ExifInterface.TAG_DATETIME),
                    iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY),
                    fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER),
                    exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
                    focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
                    hasGps = latLong != null
                )
            } ?: ExifInfo(null, null, null, null, null, null, null, false)
        } catch (e: Exception) {
            ExifInfo(null, null, null, null, null, null, null, false)
        }
    }

    suspend fun stripExif(sourceBitmap: Bitmap, quality: Int = 95): ByteArray = withContext(Dispatchers.Default) {
        val stream = ByteArrayOutputStream()
        // Fresh JPEG compression without EXIF tags strips all metadata
        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(10, 100), stream)
        stream.toByteArray()
    }
}
