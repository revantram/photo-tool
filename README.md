# Photo Tool — Offline Pro Studio 📸

[![Build Photo Tool APK](https://github.com/revantram/photo-tool/actions/workflows/build-apk.yml/badge.svg)](https://github.com/revantram/photo-tool/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Photo Tool** is a 100% offline-first Android application built with **Kotlin + Jetpack Compose** designed for exams, government ID card preparation, image utilities, PDF conversion, privacy blurring, and document scanning with zero cloud dependencies or paid API keys.

---

## 🌟 Key Features

### 1. ID Card, Exam & Document Utilities
- **ID Card Front + Back Merger:**
  - Aadhaar, PAN, Voter ID, Driving License & CR80 ratio (`85.6mm × 54mm`).
  - Dual-slot import (`+ Front Side`, `+ Back Side`).
  - Layout modes: **Top-Bottom Vertical**, **Side-by-Side Horizontal**, and **A4 Printable Sheet Layout** with dashed cutting lines and optional **"Self-Attested"** watermark.
  - Direct export as **JPG / PNG** or printable **A4 PDF**.
- **Signature Cleaner & Maker:**
  - Isolate handwritten ink from paper, smooth and boost dark contrast.
  - Export as **100% Transparent PNG** or **Pure White JPG**.
- **Photo + Signature Merge (Exam Form Joiner):**
  - Combine passport photo on top and signature at bottom with custom px dimensions (e.g. `200×250 px`, `300×400 px`) and exact KB size caps.
- **Passport / ID Photo Maker & A4 Sheet:**
  - Standard passport sizes (Indian 35×45mm, US Visa 2×2 in, Stamp size).
  - Multi-copy **A4 Printable Sheet** generator (4, 8, 12, 16 copies) with cutting guides.
- **Image to PDF & PDF to Image:**
  - Merge multiple images into a compact PDF using native Android `PdfDocument`.
  - Extract PDF pages into crisp HD JPG/PNG images via `PdfRenderer`.
- **Offline OCR (Text Extractor):**
  - Google ML Kit Text Recognition v2 bundled offline models for **Hindi/Devanagari** and **English/Latin** text extraction with 1-tap copy & share.

### 2. Core Editing & Privacy Tools
- **Exact KB Target Compressor:**
  - Binary-search quality & dimension reduction loop to hit an exact user-entered KB target (e.g., `20 KB`, `50 KB`, `100 KB`).
- **Crop, Rotate & Flip:**
  - Presets for `1:1`, `4:5`, `16:9`, `A4`, 90° rotate, and horizontal/vertical flip.
- **Color & Light Adjustments:**
  - Real-time sliders for Brightness, Contrast, Saturation, Exposure, and Warmth.
- **Privacy Blur & Pixelate Brush:**
  - Mosaic pixelation, Gaussian blur, and solid blackout markers to redact sensitive Aadhaar/ID numbers and faces.
- **Custom Watermark:**
  - Custom text watermarking with opacity, angle, and tile mode.

### 3. Batch, Inspector & Security Utilities
- **Batch Processing:**
  - Batch compress multiple photos to an exact KB limit in background coroutines.
- **QR Code & Barcode Scanner / Generator:**
  - High-res QR generator (ZXing) + Offline Barcode scanner (ML Kit).
- **EXIF Inspector & Cleaner:**
  - View camera model, ISO, date, GPS metadata; 1-tap **"Strip All EXIF Metadata"** for privacy-safe sharing.
- **ZIP Archive Creator:**
  - Bundle multiple images into a standard `.zip` archive for easy sharing.
- **Scoped Storage & Cache Cleaner:**
  - Compliant with Android 10–15 (`Pictures/PhotoTool/`).
  - 1-tap temporary cache cleaner in Settings with freed MB indicator.

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose with Material 3 (True OLED Black `#000000` design system)
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 15 (API 35)
- **Architecture:** Clean MVVM (`ViewModel`, `StateFlow`, Kotlin Coroutines `Dispatchers.Default` & `IO`)
- **Image Processing:** Native `android.graphics.Bitmap`, `Canvas`, `ColorMatrix`, `PdfDocument`, `PdfRenderer`
- **Offline ML:**
  - `play-services-mlkit-text-recognition`
  - `play-services-mlkit-text-recognition-devanagari`
  - `play-services-mlkit-barcode-scanning`
- **QR Generation:** ZXing Core `3.5.3`

---

## 🚀 Building & Generating APK

### Automated Build via GitHub Actions
Every commit pushed to `main` or `master` triggers the `.github/workflows/build-apk.yml` workflow:
1. Checks out repository.
2. Sets up JDK 17 & Android SDK.
3. Compiles `./gradlew assembleDebug`.
4. Produces `PhotoTool-v1.0.apk` and uploads it to GitHub Actions Artifacts for direct 1-tap download.

### Local Build (Command Line)
```bash
# Grant execution permission
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Privacy & Permissions
- **Zero Internet Required:** All image and OCR processing happens on-device.
- **Scoped Storage:** Output files are saved to `Pictures/PhotoTool/` via `MediaStore`.
- **Share to Edit Intent:** Accepts images shared from WhatsApp or Gallery directly into Photo Tool.
