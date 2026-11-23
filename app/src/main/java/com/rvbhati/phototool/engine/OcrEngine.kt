package com.rvbhati.phototool.engine

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OcrEngine {

    suspend fun extractText(bitmap: Bitmap, isDevanagari: Boolean = true): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val recognizer = if (isDevanagari) {
                    TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
                } else {
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                }

                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { e ->
                        continuation.resume("Error during text extraction: ${e.localizedMessage}")
                    }
            } catch (e: Exception) {
                continuation.resume("Failed to initialize offline OCR: ${e.localizedMessage}")
            }
        }
}
