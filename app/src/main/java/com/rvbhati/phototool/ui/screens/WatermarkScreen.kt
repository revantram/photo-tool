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
import com.rvbhati.phototool.engine.WatermarkEngine
import com.rvbhati.phototool.engine.WatermarkOptions
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var watermarkedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var opacity by remember { mutableFloatStateOf(0.4f) }
    var angle by remember { mutableFloatStateOf(-30f) }
    var tileMode by remember { mutableStateOf(true) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                sourceBitmap = bmp
                watermarkedBitmap = bmp
            }
        }
    }

    fun applyWatermark() {
        val src = sourceBitmap ?: return
        scope.launch {
            watermarkedBitmap = WatermarkEngine.addTextWatermark(
                source = src,
                options = WatermarkOptions(
                    text = watermarkText,
                    opacity = opacity,
                    angle = angle,
                    tileMode = tileMode
                )
            )
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Custom Watermark", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            if (watermarkedBitmap == null) {
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
                        Icon(Icons.Rounded.BrandingWatermark, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo to Watermark", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = watermarkedBitmap!!.asImageBitmap(),
                        contentDescription = "Watermarked Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it; applyWatermark() },
                    label = { Text("Watermark Text") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tile across whole image", color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(
                        checked = tileMode,
                        onCheckedChange = { tileMode = it; applyWatermark() },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Opacity: ${(opacity * 100).toInt()}%", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it; applyWatermark() },
                    valueRange = 0.1f..0.9f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                )

                Text("Angle: ${angle.toInt()}°", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = angle,
                    onValueChange = { angle = it; applyWatermark() },
                    valueRange = -90f..90f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val bmp = watermarkedBitmap ?: return@Button
                            scope.launch {
                                val uri = MediaStoreShareHelper.saveBitmapToGallery(context, bmp, "Watermarked")
                                if (uri != null) {
                                    Toast.makeText(context, "Saved to Pictures/PhotoTool!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Save Photo", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            sourceBitmap?.let { watermarkedBitmap = it }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Text("Reset", color = TextSecondary)
                    }
                }
            }
        }
    }
}
