package com.rvbhati.phototool.engine

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

object MediaStoreShareHelper {

    private const val SUB_FOLDER = "PhotoTool"

    suspend fun saveBytesToGallery(
        context: Context,
        bytes: ByteArray,
        displayName: String,
        mimeType: String = "image/jpeg"
    ): Uri? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$SUB_FOLDER"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { out: OutputStream ->
                    out.write(bytes)
                    out.flush()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                null
            }
        } else {
            null
        }
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        baseFileName: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 92
    ): Uri? = withContext(Dispatchers.Default) {
        val ext = when (format) {
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP_LOSSY, Bitmap.CompressFormat.WEBP_LOSSLESS -> "webp"
            else -> "jpg"
        }
        val mime = when (format) {
            Bitmap.CompressFormat.PNG -> "image/png"
            Bitmap.CompressFormat.WEBP_LOSSY, Bitmap.CompressFormat.WEBP_LOSSLESS -> "image/webp"
            else -> "image/jpeg"
        }

        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(format, quality.coerceIn(10, 100), baos)
        val fileName = "${baseFileName}_${System.currentTimeMillis()}.$ext"
        saveBytesToGallery(context, baos.toByteArray(), fileName, mime)
    }

    fun shareFile(
        context: Context,
        file: File,
        mimeType: String = "application/pdf",
        whatsappOnly: Boolean = false
    ) {
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (whatsappOnly) {
                setPackage("com.whatsapp")
            }
        }

        try {
            if (whatsappOnly) {
                context.startActivity(shareIntent)
            } else {
                val chooser = Intent.createChooser(shareIntent, "Share via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: ActivityNotFoundException) {
            val fallback = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share file"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun shareImageUri(
        context: Context,
        imageUri: Uri,
        mimeType: String = "image/jpeg",
        whatsappOnly: Boolean = false
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (whatsappOnly) {
                setPackage("com.whatsapp")
            }
        }

        try {
            if (whatsappOnly) {
                context.startActivity(intent)
            } else {
                val chooser = Intent.createChooser(intent, "Share Photo")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: ActivityNotFoundException) {
            val fallback = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share Photo"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }
}
