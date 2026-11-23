package com.rvbhati.phototool.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class QuickToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
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
    val oledBlack = Color(0xFF000000)
    val glassSurface = Color(0xFF121418)
    val glassBorder = Color(0x26FFFFFF)

    val quickTools = listOf(
        QuickToolItem("id_merger", "ID Card Merger", "Front + Back A4", Icons.Rounded.Badge, Color(0xFF38BDF8)),
        QuickToolItem("kb_compress", "KB Compress", "Exact KB Target", Icons.Rounded.Compress, Color(0xFF4ADE80)),
        QuickToolItem("sign_maker", "Signature Clean", "White / Transparent", Icons.Rounded.Draw, Color(0xFFF472B6)),
        QuickToolItem("photo_sign", "Photo + Sign", "Exam Form Joiner", Icons.Rounded.Portrait, Color(0xFFFB923C)),
        QuickToolItem("img_to_pdf", "Image ↔ PDF", "Merge & Extract", Icons.Rounded.PictureAsPdf, Color(0xFFF87171)),
        QuickToolItem("ocr_text", "OCR Extract", "Hindi & English", Icons.Rounded.DocumentScanner, Color(0xFFA78BFA)),
        QuickToolItem("crop_resize", "Crop & Resize", "Px, Cm & Ratios", Icons.Rounded.Crop, Color(0xFF2DD4BF)),
        QuickToolItem("adjust_filters", "Color & Light", "Contrast & Brightness", Icons.Rounded.Tune, Color(0xFFF59E0B)),
        QuickToolItem("bg_remove", "Privacy Brush", "Blur & Pixelate", Icons.Rounded.AutoFixHigh, Color(0xFFFACC15)),
        QuickToolItem("passport_a4", "Passport Sheet", "A4 / 4x6 Copies", Icons.Rounded.GridView, Color(0xFF60A5FA)),
        QuickToolItem("watermark", "Watermark", "Text & Tile Mode", Icons.Rounded.BrandingWatermark, Color(0xFF818CF8)),
        QuickToolItem("batch_edit", "Batch Process", "Multi-Photo Edit", Icons.Rounded.Layers, Color(0xFF34D399)),
        QuickToolItem("qr_tool", "QR Scan / Gen", "Read & Create QR", Icons.Rounded.QrCodeScanner, Color(0xFFE879F9)),
        QuickToolItem("exif_zip", "EXIF Cleaner", "Privacy Metadata", Icons.Rounded.FolderZip, Color(0xFF94A3B8)),
        QuickToolItem("zip_pack", "ZIP Creator", "Bundle Images", Icons.Rounded.Archive, Color(0xFF38BDF8))
    )

    Scaffold(
        containerColor = oledBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = oledBlack.copy(alpha = 0.88f),
                    titleContentColor = Color.White
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
                                text = "Photo Tool",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Offline Pro Studio",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 24.dp,
                start = 14.dp,
                end = 14.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Card: Select Photo & Camera
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = glassSurface,
                    border = BorderStroke(1.dp, glassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E293B).copy(alpha = 0.65f), glassSurface)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Start Editing Instantly",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Crop, Compress KB, ID Merge, PDF & OCR — 100% Offline",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onSelectGallery,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB)
                                ),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(50.dp)
                            ) {
                                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select Photo", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            OutlinedButton(
                                onClick = onOpenCamera,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0x4DFFFFFF)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Section Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "All Utility Tools",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }

            // Tool Cards
            items(quickTools) { tool ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = glassSurface,
                    border = BorderStroke(1.dp, glassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onToolClick(tool.id) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(tool.accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.title,
                                tint = tool.accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = tool.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = tool.subtitle,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
