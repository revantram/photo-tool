package com.rvbhati.phototool.engine

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class CardMergeLayout {
    VERTICAL_STACK,
    HORIZONTAL_SIDE_BY_SIDE,
    A4_PRINTABLE_SHEET
}

object IdCardMergerEngine {

    // At 300 DPI: A4 = 2480 x 3508 px, CR80 ID (85.6mm x 54mm) = 1011 x 638 px
    private const val A4_WIDTH_300DPI = 2480
    private const val A4_HEIGHT_300DPI = 3508
    private const val CARD_W_300DPI = 1011
    private const val CARD_H_300DPI = 638

    suspend fun mergeIdCard(
        frontBitmap: Bitmap,
        backBitmap: Bitmap,
        layout: CardMergeLayout,
        gapPx: Int = 36,
        drawCardBorder: Boolean = true,
        selfAttestedText: String? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        when (layout) {
            CardMergeLayout.VERTICAL_STACK -> {
                val cardW = CARD_W_300DPI
                val cardH = CARD_H_300DPI
                val extraNoteHeight = if (!selfAttestedText.isNullOrBlank()) 90 else 0
                val totalW = cardW + gapPx * 2
                val totalH = cardH * 2 + gapPx * 3 + extraNoteHeight

                val out = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                canvas.drawColor(Color.WHITE)

                val frontScaled = Bitmap.createScaledBitmap(frontBitmap, cardW, cardH, true)
                val backScaled = Bitmap.createScaledBitmap(backBitmap, cardW, cardH, true)

                canvas.drawBitmap(frontScaled, gapPx.toFloat(), gapPx.toFloat(), null)
                val secondY = (gapPx * 2 + cardH).toFloat()
                canvas.drawBitmap(backScaled, gapPx.toFloat(), secondY, null)

                if (drawCardBorder) {
                    drawBorder(canvas, gapPx.toFloat(), gapPx.toFloat(), cardW.toFloat(), cardH.toFloat())
                    drawBorder(canvas, gapPx.toFloat(), secondY, cardW.toFloat(), cardH.toFloat())
                }

                if (!selfAttestedText.isNullOrBlank()) {
                    drawBottomNote(canvas, selfAttestedText, totalW.toFloat(), (totalH - 28).toFloat())
                }

                frontScaled.recycle()
                backScaled.recycle()
                out
            }

            CardMergeLayout.HORIZONTAL_SIDE_BY_SIDE -> {
                val cardW = CARD_W_300DPI
                val cardH = CARD_H_300DPI
                val extraNoteHeight = if (!selfAttestedText.isNullOrBlank()) 90 else 0
                val totalW = cardW * 2 + gapPx * 3
                val totalH = cardH + gapPx * 2 + extraNoteHeight

                val out = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                canvas.drawColor(Color.WHITE)

                val frontScaled = Bitmap.createScaledBitmap(frontBitmap, cardW, cardH, true)
                val backScaled = Bitmap.createScaledBitmap(backBitmap, cardW, cardH, true)

                val secondX = (gapPx * 2 + cardW).toFloat()
                canvas.drawBitmap(frontScaled, gapPx.toFloat(), gapPx.toFloat(), null)
                canvas.drawBitmap(backScaled, secondX, gapPx.toFloat(), null)

                if (drawCardBorder) {
                    drawBorder(canvas, gapPx.toFloat(), gapPx.toFloat(), cardW.toFloat(), cardH.toFloat())
                    drawBorder(canvas, secondX, gapPx.toFloat(), cardW.toFloat(), cardH.toFloat())
                }

                if (!selfAttestedText.isNullOrBlank()) {
                    drawBottomNote(canvas, selfAttestedText, totalW.toFloat(), (totalH - 28).toFloat())
                }

                frontScaled.recycle()
                backScaled.recycle()
                out
            }

            CardMergeLayout.A4_PRINTABLE_SHEET -> {
                val out = Bitmap.createBitmap(A4_WIDTH_300DPI, A4_HEIGHT_300DPI, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                canvas.drawColor(Color.WHITE)

                val centerX = (A4_WIDTH_300DPI - CARD_W_300DPI) / 2f
                val frontTopY = 260f
                val backTopY = frontTopY + CARD_H_300DPI + gapPx.coerceAtLeast(80)

                val frontScaled = Bitmap.createScaledBitmap(frontBitmap, CARD_W_300DPI, CARD_H_300DPI, true)
                val backScaled = Bitmap.createScaledBitmap(backBitmap, CARD_W_300DPI, CARD_H_300DPI, true)

                canvas.drawBitmap(frontScaled, centerX, frontTopY, null)
                canvas.drawBitmap(backScaled, centerX, backTopY, null)

                if (drawCardBorder) {
                    drawBorder(canvas, centerX, frontTopY, CARD_W_300DPI.toFloat(), CARD_H_300DPI.toFloat())
                    drawBorder(canvas, centerX, backTopY, CARD_W_300DPI.toFloat(), CARD_H_300DPI.toFloat())
                }

                if (!selfAttestedText.isNullOrBlank()) {
                    drawBottomNote(
                        canvas = canvas,
                        text = selfAttestedText,
                        canvasWidth = A4_WIDTH_300DPI.toFloat(),
                        yPos = backTopY + CARD_H_300DPI + 75f
                    )
                }

                frontScaled.recycle()
                backScaled.recycle()
                out
            }
        }
    }

    private fun drawBorder(canvas: Canvas, left: Float, top: Float, width: Float, height: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(14f, 10f), 0f)
        }
        canvas.drawRect(left, top, left + width, top + height, paint)
    }

    private fun drawBottomNote(canvas: Canvas, text: String, canvasWidth: Float, yPos: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(text, canvasWidth / 2f, yPos, paint)
    }
}
