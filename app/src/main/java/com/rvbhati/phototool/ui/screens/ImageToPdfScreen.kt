package com.rvbhati.phototool.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvbhati.phototool.engine.BitmapUtils
import com.rvbhati.phototool.engine.ImageToPdfEngine
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.engine.PdfExportResult
import com.rvbhati.phototool.engine.PdfPageMode
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val images = remember { mutableStateListOf<Bitmap>() }
    var pageMode by remember { mutableStateOf(PdfPageMode.A4_FIT_WITH_MARGIN) }
    var jpegQuality by remember { mutableIntStateOf(85) }
    var isGenerating by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<PdfExportResult?>(null) }

    val pickMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        scope.launch {
            for (uri in uris) {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                if (bmp != null) {
                    images.add(bmp)
                }
            }
            exportResult = null
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Image to PDF Converter", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Button(
                onClick = { pickMultipleLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Images for PDF (${images.size} added)", fontWeight = FontWeight.Bold)
            }

            if (images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Selected Pages (${images.size}):", color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    itemsIndexed(images) { index, bmp ->
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Page $index",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { images.removeAt(index) },
                                modifier = Modifier.align(Alignment.TopEnd).size(26.dp)
                            ) {
                                Icon(Icons.Rounded.Cancel, contentDescription = "Remove", tint = AccentRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("PDF Page Mode", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = pageMode == PdfPageMode.A4_FIT_WITH_MARGIN,
                        onClick = { pageMode = PdfPageMode.A4_FIT_WITH_MARGIN },
                        label = { Text("A4 Fit With Margin") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentRed)
                    )
                    FilterChip(
                        selected = pageMode == PdfPageMode.MATCH_IMAGE_SIZE,
                        onClick = { pageMode = PdfPageMode.MATCH_IMAGE_SIZE },
                        label = { Text("Original Dimensions") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentRed)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Compression Quality: $jpegQuality%", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = jpegQuality.toFloat(),
                    onValueChange = { jpegQuality = it.toInt() },
                    valueRange = 30f..100f,
                    colors = SliderDefaults.colors(thumbColor = AccentRed, activeTrackColor = AccentRed)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        scope.launch {
                            val outFile = File(context.cacheDir, "PhotoTool_${System.currentTimeMillis()}.pdf")
                            exportResult = ImageToPdfEngine.createPdfFromBitmaps(
                                images = images.toList(),
                                outputFile = outFile,
                                pageMode = pageMode,
                                jpegQuality = jpegQuality
                            )
                            isGenerating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextPrimary)
                    } else {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create & Export PDF", fontWeight = FontWeight.Bold)
                    }
                }

                exportResult?.let { res ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("PDF Generated Successfully!", color = AccentRed, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pages: ${res.pageCount} | Size: ${"%.1f".format(res.sizeKb)} KB", color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        MediaStoreShareHelper.shareFile(context, res.outputFile, "application/pdf")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share PDF")
                                }
                                Button(
                                    onClick = {
                                        MediaStoreShareHelper.shareFile(context, res.outputFile, "application/pdf", whatsappOnly = true)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                ) {
                                    Text("WhatsApp", color = OledBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
