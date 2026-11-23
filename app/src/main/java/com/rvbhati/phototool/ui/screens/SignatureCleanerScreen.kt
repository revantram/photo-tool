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
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.engine.SignatureCleanerEngine
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureCleanerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cleanedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var transparentBg by remember { mutableStateOf(true) }
    var inkThreshold by remember { mutableFloatStateOf(0.65f) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                sourceBitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                cleanedBitmap = null
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Signature Cleaner & Maker", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Rounded.Draw, contentDescription = null, tint = AccentPink, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                        ) {
                            Text("Select Paper Signature Photo", color = OledBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (transparentBg) DarkSurfaceElevated else androidx.compose.ui.graphics.Color.White)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val displayBmp = cleanedBitmap ?: sourceBitmap!!
                    Image(
                        bitmap = displayBmp.asImageBitmap(),
                        contentDescription = "Signature Preview",
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Threshold Slider
                Text("Ink Darkness Threshold: ${(inkThreshold * 100).toInt()}%", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = inkThreshold,
                    onValueChange = { inkThreshold = it },
                    valueRange = 0.3f..0.9f,
                    colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink)
                )

                // Background Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transparent Background (PNG)", color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(
                        checked = transparentBg,
                        onCheckedChange = { transparentBg = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentPink)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val src = sourceBitmap ?: return@Button
                        isProcessing = true
                        scope.launch {
                            cleanedBitmap = SignatureCleanerEngine.cleanSignature(
                                source = src,
                                transparentBackground = transparentBg,
                                inkThreshold = inkThreshold
                            )
                            isProcessing = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OledBlack)
                    } else {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clean & Extract Ink", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                }

                cleanedBitmap?.let { cleaned ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val format = if (transparentBg) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                                    val uri = MediaStoreShareHelper.saveBitmapToGallery(context, cleaned, "Cleaned_Signature", format = format)
                                    if (uri != null) {
                                        Toast.makeText(context, "Saved to Pictures/PhotoTool!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Save (${if (transparentBg) "PNG" else "JPG"})", color = OledBlack, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val format = if (transparentBg) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                                    val uri = MediaStoreShareHelper.saveBitmapToGallery(context, cleaned, "Cleaned_Signature", format = format)
                                    if (uri != null) {
                                        val mime = if (transparentBg) "image/png" else "image/jpeg"
                                        MediaStoreShareHelper.shareImageUri(context, uri, mime)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}
