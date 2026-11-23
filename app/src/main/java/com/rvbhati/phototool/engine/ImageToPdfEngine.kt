package com.rvbhati.phototool.engine

import android.graphics.*
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

enum class PdfPageMode {
    A4_FIT_WITH_MARGIN,
    MATCH_IMAGE_SIZE
}

data class PdfExportResult(
    val outputFile: File,
    val pageCount: Int,
    val sizeKb: Double
)

object ImageToPdfEngine {

    // Standard A4 in PDF points (72 DPI): 8.27in x 11.69in = 595 x 842 pt
    private const val A4_WIDTH_PT = 595
    private const val A4_HEIGHT_PT = 842

    suspend fun createPdfFromBitmaps(
        images: List<Bitmap>,
        outputFile: File,
        pageMode: PdfPageMode = PdfPageMode.A4_FIT_WITH_MARGIN,
        marginPt: Int = 24,
        jpegQuality: Int = 82
    ): PdfExportResult = withContext(Dispatchers.IO) {
        require(images.isNotEmpty()) { "At least one image is required to generate PDF." }

        outputFile.parentFile?.mkdirs()
        val pdfDocument = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        try {
            images.forEachIndexed { index, bitmap ->
                val (pageW, pageH) = when (pageMode) {
                    PdfPageMode.A4_FIT_WITH_MARGIN -> A4_WIDTH_PT to A4_HEIGHT_PT
                    PdfPageMode.MATCH_IMAGE_SIZE -> {
                        val w = bitmap.width.coerceAtLeast(100)
                        val h = bitmap.height.coerceAtLeast(100)
                        w to h
                    }
                }

                val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                val safeMargin = if (pageMode == PdfPageMode.A4_FIT_WITH_MARGIN) {
                    marginPt.coerceIn(0, 100).toFloat()
                } else {
                    0f
                }

                val availW = (pageW - safeMargin * 2f).coerceAtLeast(20f)
                val availH = (pageH - safeMargin * 2f).coerceAtLeast(20f)

                val scale = min(availW / bitmap.width.toFloat(), availH / bitmap.height.toFloat())
                val drawW = bitmap.width * scale
                val drawH = bitmap.height * scale
                val left = safeMargin + (availW - drawW) / 2f
                val top = safeMargin + (availH - drawH) / 2f

                // Compress intermediate bitmap to keep PDF size compact & fast
                val jpgStream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality.coerceIn(20, 98), jpgStream)
                val jpgBytes = jpgStream.toByteArray()
                val compressedBmp = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.size)

                val destRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(compressedBmp, null, destRect, paint)
                compressedBmp.recycle()

                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
        } finally {
            pdfDocument.close()
        }

        val sizeKb = if (outputFile.exists()) outputFile.length() / 1024.0 else 0.0
        PdfExportResult(
            outputFile = outputFile,
            pageCount = images.size,
            sizeKb = sizeKb
        )
    }
}
