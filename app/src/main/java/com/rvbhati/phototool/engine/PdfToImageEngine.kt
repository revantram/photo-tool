package com.rvbhati.phototool.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ExtractedPdfPage(
    val pageIndex: Int,
    val pageNumber: Int,
    val outputFile: File
)

object PdfToImageEngine {

    suspend fun extractPagesToImages(
        context: Context,
        pdfUri: Uri,
        outputDir: File,
        scaleMultiplier: Float = 2.0f,
        asPng: Boolean = false,
        jpegQuality: Int = 92
    ): List<ExtractedPdfPage> = withContext(Dispatchers.IO) {
        outputDir.mkdirs()
        val extracted = mutableListOf<ExtractedPdfPage>()

        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IllegalArgumentException("Unable to open PDF file.")

        pfd.use { descriptor ->
            val renderer = PdfRenderer(descriptor)
            renderer.use { pdfRenderer ->
                val safeScale = scaleMultiplier.coerceIn(1.0f, 4.0f)

                for (index in 0 until pdfRenderer.pageCount) {
                    pdfRenderer.openPage(index).use { page ->
                        val width = (page.width * safeScale).toInt().coerceAtLeast(1)
                        val height = (page.height * safeScale).toInt().coerceAtLeast(1)

                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)

                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )

                        val ext = if (asPng) "png" else "jpg"
                        val outFile = File(outputDir, "pdf_page_${index + 1}.$ext")

                        FileOutputStream(outFile).use { fos ->
                            if (asPng) {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            } else {
                                bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    jpegQuality.coerceIn(10, 100),
                                    fos
                                )
                            }
                        }
                        bitmap.recycle()

                        extracted.add(
                            ExtractedPdfPage(
                                pageIndex = index,
                                pageNumber = index + 1,
                                outputFile = outFile
                            )
                        )
                    }
                }
            }
        }

        extracted
    }
}
