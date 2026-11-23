package com.rvbhati.phototool.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.rvbhati.phototool.engine.*
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportSheetScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFormat by remember { mutableStateOf(PassportFormat.INDIA_PASSPORT) }
    var copiesCount by remember { mutableIntStateOf(8) }
    var a4SheetBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                sourceBitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                a4SheetBitmap = null
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Passport Photo & A4 Sheet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (sourceBitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { pickImageLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Rounded.GridView, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Passport Photo", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sourceBitmap!!.asImageBitmap(),
                        contentDescription = "Source Passport Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Passport Size Preset", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                PassportFormat.values().forEach { fmt ->
                    FilterChip(
                        selected = selectedFormat == fmt,
                        onClick = { selectedFormat = fmt; a4SheetBitmap = null },
                        label = { Text(fmt.title) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Copies on A4 Sheet: $copiesCount copies", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4, 8, 12, 16).forEach { count ->
                        FilterChip(
                            selected = copiesCount == count,
                            onClick = { copiesCount = count; a4SheetBitmap = null },
                            label = { Text("$count Copies") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val bmp = sourceBitmap ?: return@Button
                        isGenerating = true
                        scope.launch {
                            val single = PassportSheetEngine.createSinglePassport(bmp, selectedFormat)
                            a4SheetBitmap = PassportSheetEngine.generateA4PrintableSheet(single, copiesCount)
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
                        Icon(Icons.Rounded.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate A4 Printable Sheet", fontWeight = FontWeight.Bold)
                    }
                }

                a4SheetBitmap?.let { sheet ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("A4 Sheet Preview ($copiesCount copies with cut lines)", color = AccentCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = sheet.asImageBitmap(),
                                contentDescription = "A4 Sheet",
                                modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val uri = MediaStoreShareHelper.saveBitmapToGallery(context, sheet, "Passport_A4_Sheet")
                                            if (uri != null) {
                                                Toast.makeText(context, "Saved A4 Sheet to Gallery!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                ) {
                                    Text("Save Image", color = OledBlack, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val pdfFile = File(context.cacheDir, "Passport_A4_${System.currentTimeMillis()}.pdf")
                                            ImageToPdfEngine.createPdfFromBitmaps(listOf(sheet), pdfFile)
                                            MediaStoreShareHelper.shareFile(context, pdfFile, "application/pdf")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                                ) {
                                    Text("Export PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
