package com.rvbhati.phototool.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object QrCodeEngine {

    suspend fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE
    ): Bitmap = withContext(Dispatchers.Default) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
            }
        }

        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    suspend fun scanBarcodeFromBitmap(bitmap: Bitmap): String? = suspendCancellableCoroutine { continuation ->
        try {
            val scanner = BarcodeScanning.getClient()
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val firstBarcode = barcodes.firstOrNull()?.rawValue
                    continuation.resume(firstBarcode)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }
}
