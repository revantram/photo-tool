package com.rvbhati.phototool.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvbhati.phototool.AppState

data class QuickToolItem(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val subtitleHindi: String,
    val subtitleEnglish: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoToolHomeScreen(
    onSelectGallery: () -> Unit,
    onOpenCamera: () -> Unit,
    onToolClick: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val isDark = AppState.isDarkMode
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF1F5F9)
    val cardColor = if (isDark) Color(0xFF121418) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0x26FFFFFF) else Color(0x1F000000)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Crop & Resize placed at the end (last) as requested
    val quickTools = listOf(
        QuickToolItem("id_merger", "आईडी कार्ड", "ID Merger", "आगे + पीछे", "Front + Back", Icons.Rounded.Badge, Color(0xFF38BDF8)),
        QuickToolItem("kb_compress", "केबी कम्प्रेस", "KB Compress", "मनचाही साइज", "Exact Target", Icons.Rounded.Compress, Color(0xFF4ADE80)),
        QuickToolItem("sign_maker", "हस्ताक्षर साफ", "Sign Clean", "सफेद / पारदर्शी", "Transparent", Icons.Rounded.Draw, Color(0xFFF472B6)),
        QuickToolItem("photo_sign", "फोटो + साइन", "Photo + Sign", "परीक्षा फॉर्म", "Exam Joiner", Icons.Rounded.Portrait, Color(0xFFFB923C)),
        QuickToolItem("img_to_pdf", "फोटो ↔ PDF", "Image ↔ PDF", "मर्ज व बनाएं", "Merge & Read", Icons.Rounded.PictureAsPdf, Color(0xFFF87171)),
        QuickToolItem("ocr_text", "टेक्स्ट निकालें", "OCR Extract", "फोटो से लिखावट", "Hindi & Eng", Icons.Rounded.DocumentScanner, Color(0xFFA78BFA)),
        QuickToolItem("adjust_filters", "रंग व रोशनी", "Color & Light", "चमक व कंट्रास्ट", "Adjust Tone", Icons.Rounded.Tune, Color(0xFFF59E0B)),
        QuickToolItem("bg_remove", "प्राइवेसी ब्लर", "Privacy Blur", "छुपाएं व ब्लर", "Blur & Hide", Icons.Rounded.AutoFixHigh, Color(0xFFFACC15)),
        QuickToolItem("passport_a4", "पासपोर्ट शीट", "Passport A4", "ए4 प्रिंटेबल", "A4 / 4x6 Copies", Icons.Rounded.GridView, Color(0xFF60A5FA)),
        QuickToolItem("watermark", "वाटरमार्क", "Watermark", "नाम व लोगो", "Text & Tile", Icons.Rounded.BrandingWatermark, Color(0xFF818CF8)),
        QuickToolItem("batch_edit", "एक साथ कई", "Batch Edit", "बैच कम्प्रेस", "Multi-Photo", Icons.Rounded.Layers, Color(0xFF34D399)),
        QuickToolItem("qr_tool", "क्यूआर कोड", "QR Tool", "स्कैन व बनाएं", "Scan & Make", Icons.Rounded.QrCodeScanner, Color(0xFFE879F9)),
        QuickToolItem("exif_zip", "डाटा हटाएं", "EXIF Clean", "प्राइवेसी सुरक्षा", "Strip Exif", Icons.Rounded.FolderZip, Color(0xFF94A3B8)),
        QuickToolItem("zip_pack", "ज़िप बनाएं", "ZIP Creator", "फाइल बंडल", "Pack Photos", Icons.Rounded.Archive, Color(0xFF38BDF8)),
        QuickToolItem("crop_resize", "फोटो क्रॉप", "Photo Crop", "काटें व साइज", "Crop & Resize", Icons.Rounded.Crop, Color(0xFF2DD4BF))
    )

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor.copy(alpha = 0.92f),
                    titleContentColor = textPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoFilter,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppState.t("फोटो टूल", "Photo Tool"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = AppState.t("ऑफलाइन प्रो स्टूडियो", "Offline Pro Studio"),
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Small circular Day / Night Theme Toggle Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                            .border(1.dp, borderColor, CircleShape)
                            .clickable {
                                AppState.isDarkMode = !AppState.isDarkMode
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDark) Color(0xFFFACC15) else Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = textPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2x2 layout as requested
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 28.dp,
                start = 14.dp,
                end = 14.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 2x2 Grid Tool Cards (Photo & Camera banner removed, photo crop at last)
            items(quickTools) { tool ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToolClick(tool.id) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(tool.accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.titleHindi,
                                tint = tool.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (AppState.isHindi) tool.titleHindi else tool.titleEnglish,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (AppState.isHindi) tool.subtitleHindi else tool.subtitleEnglish,
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
