package com.rvbhati.phototool.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object IdMerger : Screen("id_merger")
    object KbCompress : Screen("kb_compress")
    object SignatureCleaner : Screen("sign_maker")
    object PhotoSignatureJoiner : Screen("photo_sign")
    object ImageToPdf : Screen("img_to_pdf")
    object PdfToImage : Screen("pdf_to_img")
    object OcrExtract : Screen("ocr_text")
    object CropResize : Screen("crop_resize")
    object AdjustFilters : Screen("adjust_filters")
    object PassportSheet : Screen("passport_a4")
    object PrivacyBlur : Screen("bg_remove")
    object Watermark : Screen("watermark")
    object QrTool : Screen("qr_tool")
    object ExifCleaner : Screen("exif_zip")
    object BatchEdit : Screen("batch_edit")
    object ZipPack : Screen("zip_pack")
    object Settings : Screen("settings")
}
