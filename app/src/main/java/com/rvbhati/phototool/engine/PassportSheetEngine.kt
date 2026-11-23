package com.rvbhati.phototool.engine

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PassportFormat(val title: String, val widthMm: Float, val heightMm: Float) {
    INDIA_PASSPORT("Indian Passport (35x45 mm)", 35f, 45f),
    US_VISA("US Visa / Passport (2x2 inch)", 50.8f, 50.8f),
    STAMP_SIZE("Stamp Size (25x30 mm)", 25f, 30f)
}

object PassportSheetEngine {

    private const val A4_WIDTH_300DPI = 2480
    private const val A4_HEIGHT_300DPI = 3508
    private const val MM_TO_PX_300DPI = 300f / 25.4f // ~11.81 px/mm

    suspend fun createSinglePassport(
        source: Bitmap,
        format: PassportFormat,
        borderWidthPx: Int = 4,
        borderColor: Int = Color.parseColor("#E2E8F0")
    ): Bitmap = withContext(Dispatchers.Default) {
        val targetW = (format.widthMm * MM_TO_PX_300DPI).toInt()
        val targetH = (format.heightMm * MM_TO_PX_300DPI).toInt()

        val output = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)

        val destRect = RectF(
            borderWidthPx.toFloat(),
            borderWidthPx.toFloat(),
            (targetW - borderWidthPx).toFloat(),
            (targetH - borderWidthPx).toFloat()
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, null, destRect, paint)

        if (borderWidthPx > 0) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = borderWidthPx.toFloat()
            }
            canvas.drawRect(destRect, borderPaint)
        }

        output
    }

    suspend fun generateA4PrintableSheet(
        passportBitmap: Bitmap,
        copiesCount: Int = 8,
        drawCutLines: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        val sheet = Bitmap.createBitmap(A4_WIDTH_300DPI, A4_HEIGHT_300DPI, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.WHITE)

        val cardW = passportBitmap.width
        val cardH = passportBitmap.height

        val cols = when {
            copiesCount <= 4 -> 2
            copiesCount <= 8 -> 4
            copiesCount <= 16 -> 4
            else -> 4
        }
        val rows = (copiesCount + cols - 1) / cols

        val gapX = 40
        val gapY = 50
        val totalGridW = cols * cardW + (cols - 1) * gapX
        val startX = (A4_WIDTH_300DPI - totalGridW) / 2f
        val startY = 240f

        val cutLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
        }

        var rendered = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (rendered >= copiesCount) break

                val left = startX + c * (cardW + gapX)
                val top = startY + r * (cardH + gapY)

                canvas.drawBitmap(passportBitmap, left, top, null)

                if (drawCutLines) {
                    canvas.drawRect(left - 2, top - 2, left + cardW + 2, top + cardH + 2, cutLinePaint)
                }
                rendered++
            }
        }

        sheet
    }
}
