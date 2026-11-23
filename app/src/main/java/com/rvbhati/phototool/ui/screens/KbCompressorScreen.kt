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
import com.rvbhati.phototool.engine.BitmapUtils
import com.rvbhati.phototool.engine.CompressedResult
import com.rvbhati.phototool.engine.KbTargetCompressor
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KbCompressorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetKb by remember { mutableIntStateOf(50) }
    var isCompressing by remember { mutableStateOf(false) }
    var compressedResult by remember { mutableStateOf<CompressedResult?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                sourceBitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                compressedResult = null
            }
        }
    }

    val presets = listOf(20, 50, 100, 200, 500)

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Exact KB Target Compressor", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Select Photo from Gallery")
                        }
                    }
                }
            } else {
                // Image preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sourceBitmap!!.asImageBitmap(),
                        contentDescription = "Source Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets
                Text("Target File Size: $targetKb KB", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presets.forEach { kb ->
                        FilterChip(
                            selected = targetKb == kb,
                            onClick = { targetKb = kb },
                            label = { Text("${kb}KB") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = targetKb.toFloat(),
                    onValueChange = { targetKb = it.toInt() },
                    valueRange = 10f..1000f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val bmp = sourceBitmap ?: return@Button
                        isCompressing = true
                        scope.launch {
                            val res = KbTargetCompressor.compressToMaxKb(bmp, targetKb)
                            compressedResult = res
                            isCompressing = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isCompressing,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    if (isCompressing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OledBlack)
                    } else {
                        Icon(Icons.Rounded.Compress, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compress to ${targetKb}KB Target", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                }

                compressedResult?.let { res ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Compression Result", fontWeight = FontWeight.Bold, color = AccentGreen, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Output Size: ${"%.1f".format(res.sizeKb)} KB (Under ${targetKb} KB)", color = TextPrimary)
                            Text("Resolution: ${res.width} × ${res.height} px", color = TextSecondary)
                            Text("JPEG Quality Applied: ${res.qualityUsed}%", color = TextSecondary)

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val uri = MediaStoreShareHelper.saveBytesToGallery(
                                                context,
                                                res.bytes,
                                                "Compressed_${targetKb}KB_${System.currentTimeMillis()}.jpg"
                                            )
                                            if (uri != null) {
                                                Toast.makeText(context, "Saved to Pictures/PhotoTool!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Rounded.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save")
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val uri = MediaStoreShareHelper.saveBytesToGallery(
                                                context,
                                                res.bytes,
                                                "Share_${System.currentTimeMillis()}.jpg"
                                            )
                                            if (uri != null) {
                                                MediaStoreShareHelper.shareImageUri(context, uri)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
