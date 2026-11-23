package com.rvbhati.phototool.ui.screens

import android.graphics.Bitmap
import android.net.Uri
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
import com.rvbhati.phototool.engine.ExifInfo
import com.rvbhati.phototool.engine.ExifInspectorEngine
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExifCleanerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var exifInfo by remember { mutableStateOf<ExifInfo?>(null) }
    var isStripped by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                sourceBitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                exifInfo = ExifInspectorEngine.readExif(context, uri)
                isStripped = false
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("EXIF Metadata Inspector & Cleaner", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Rounded.FolderZip, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo to Inspect", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val bmp = sourceBitmap!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Selected Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Detected Image Metadata", color = AccentCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Resolution: ${bmp.width} × ${bmp.height} px", color = TextPrimary)
                        Text("Camera Device: ${exifInfo?.cameraMake.orEmpty()} ${exifInfo?.cameraModel.orEmpty()}".ifBlank { "Camera: Unknown" }, color = TextSecondary)
                        Text("Date Taken: ${exifInfo?.dateTaken ?: "Not available"}", color = TextSecondary)
                        Text("ISO: ${exifInfo?.iso ?: "N/A"} | F-Number: ${exifInfo?.fNumber ?: "N/A"}", color = TextSecondary)
                        Text("GPS Location Data: ${if (exifInfo?.hasGps == true) "YES (Sensitive GPS Found!)" else "None"}",
                            color = if (exifInfo?.hasGps == true) AccentRed else AccentGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val cleanBytes = ExifInspectorEngine.stripExif(bmp)
                            val uri = MediaStoreShareHelper.saveBytesToGallery(
                                context,
                                cleanBytes,
                                "Clean_NoExif_${System.currentTimeMillis()}.jpg"
                            )
                            if (uri != null) {
                                isStripped = true
                                Toast.makeText(context, "EXIF stripped! Saved clean photo to Gallery.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = OledBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Strip All EXIF & Save Clean Photo", color = OledBlack, fontWeight = FontWeight.Bold)
                }

                if (isStripped) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("✓ Clean image with zero metadata saved to Pictures/PhotoTool", color = AccentGreen, fontSize = 13.sp)
                }
            }
        }
    }
}
