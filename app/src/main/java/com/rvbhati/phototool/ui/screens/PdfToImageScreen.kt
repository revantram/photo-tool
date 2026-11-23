package com.rvbhati.phototool.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rvbhati.phototool.engine.ExtractedPdfPage
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.engine.PdfToImageEngine
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToImageScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var scaleMultiplier by remember { mutableFloatStateOf(2.0f) }
    var asPng by remember { mutableStateOf(false) }
    var isExtracting by remember { mutableStateOf(false) }
    val extractedPages = remember { mutableStateListOf<ExtractedPdfPage>() }

    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
            extractedPages.clear()
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("PDF to Image Extractor", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OledBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = { pickPdfLauncher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Rounded.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedPdfUri != null) "Change PDF File" else "Select PDF File", fontWeight = FontWeight.Bold)
            }

            if (selectedPdfUri != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Render Resolution: ${scaleMultiplier.toInt()}x HD", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = scaleMultiplier,
                    onValueChange = { scaleMultiplier = it },
                    valueRange = 1f..3f,
                    steps = 1,
                    colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                )

                Button(
                    onClick = {
                        val uri = selectedPdfUri ?: return@Button
                        isExtracting = true
                        extractedPages.clear()
                        scope.launch {
                            val outDir = File(context.cacheDir, "pdf_extracted_${System.currentTimeMillis()}")
                            val pages = PdfToImageEngine.extractPagesToImages(
                                context = context,
                                pdfUri = uri,
                                outputDir = outDir,
                                scaleMultiplier = scaleMultiplier,
                                asPng = asPng
                            )
                            extractedPages.addAll(pages)
                            isExtracting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isExtracting,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    if (isExtracting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OledBlack)
                    } else {
                        Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extract All Pages as Images", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (extractedPages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Extracted Pages (${extractedPages.size})", color = AccentGreen, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        scope.launch {
                            for (p in extractedPages) {
                                val bytes = p.outputFile.readBytes()
                                MediaStoreShareHelper.saveBytesToGallery(context, bytes, p.outputFile.name)
                            }
                            Toast.makeText(context, "Saved all ${extractedPages.size} pages to Gallery!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Save All to Gallery", color = PrimaryBlue)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                ) {
                    items(extractedPages) { page ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = page.outputFile,
                                    contentDescription = "Page ${page.pageNumber}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Page ${page.pageNumber}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        MediaStoreShareHelper.shareFile(context, page.outputFile, "image/jpeg")
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)
                                ) {
                                    Text("Share", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
