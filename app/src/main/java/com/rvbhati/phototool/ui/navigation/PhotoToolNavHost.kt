package com.rvbhati.phototool.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rvbhati.phototool.ui.home.PhotoToolHomeScreen
import com.rvbhati.phototool.ui.screens.*

@Composable
fun PhotoToolNavHost(
    navController: NavHostController,
    onSelectGallery: () -> Unit,
    onOpenCamera: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            PhotoToolHomeScreen(
                onSelectGallery = onSelectGallery,
                onOpenCamera = onOpenCamera,
                onToolClick = { toolId ->
                    when (toolId) {
                        "id_merger" -> navController.navigate(Screen.IdMerger.route)
                        "kb_compress" -> navController.navigate(Screen.KbCompress.route)
                        "sign_maker" -> navController.navigate(Screen.SignatureCleaner.route)
                        "photo_sign" -> navController.navigate(Screen.PhotoSignatureJoiner.route)
                        "img_to_pdf" -> navController.navigate(Screen.ImageToPdf.route)
                        "ocr_text" -> navController.navigate(Screen.OcrExtract.route)
                        "crop_resize" -> navController.navigate(Screen.CropResize.route)
                        "adjust_filters" -> navController.navigate(Screen.AdjustFilters.route)
                        "bg_remove" -> navController.navigate(Screen.PrivacyBlur.route)
                        "passport_a4" -> navController.navigate(Screen.PassportSheet.route)
                        "watermark" -> navController.navigate(Screen.Watermark.route)
                        "batch_edit" -> navController.navigate(Screen.BatchEdit.route)
                        "qr_tool" -> navController.navigate(Screen.QrTool.route)
                        "exif_zip" -> navController.navigate(Screen.ExifCleaner.route)
                        "zip_pack" -> navController.navigate(Screen.ZipPack.route)
                        else -> {}
                    }
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.IdMerger.route) {
            IdCardMergerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.KbCompress.route) {
            KbCompressorScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.SignatureCleaner.route) {
            SignatureCleanerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PhotoSignatureJoiner.route) {
            PhotoSignatureJoinerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ImageToPdf.route) {
            ImageToPdfScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PdfToImage.route) {
            PdfToImageScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.OcrExtract.route) {
            OcrScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.CropResize.route) {
            CropResizeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdjustFilters.route) {
            AdjustFiltersScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PrivacyBlur.route) {
            PrivacyBlurScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PassportSheet.route) {
            PassportSheetScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Watermark.route) {
            WatermarkScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.QrTool.route) {
            QrToolScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ExifCleaner.route) {
            ExifCleanerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.BatchEdit.route) {
            BatchProcessScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ZipPack.route) {
            ZipPackScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
