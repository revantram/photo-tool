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
import com.rvbhati.phototool.engine.ColorAdjustEngine
import com.rvbhati.phototool.engine.ColorFilterParams
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustFiltersScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var filteredBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var warmth by remember { mutableFloatStateOf(0f) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                sourceBitmap = bmp
                filteredBitmap = bmp
            }
        }
    }

    fun applyFilters() {
        val src = sourceBitmap ?: return
        scope.launch {
            filteredBitmap = ColorAdjustEngine.applyColorFilters(
                source = src,
                params = ColorFilterParams(
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    warmth = warmth
                )
            )
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Color & Light Adjustments", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            if (filteredBitmap == null) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
                    ) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo to Adjust", color = OledBlack, fontWeight = FontWeight.Bold)
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
                        bitmap = filteredBitmap!!.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Brightness
                Text("Brightness (${brightness.toInt()})", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it; applyFilters() },
                    valueRange = -80f..80f,
                    colors = SliderDefaults.colors(thumbColor = AccentYellow, activeTrackColor = AccentYellow)
                )

                // Contrast
                Text("Contrast (${"%.1f".format(contrast)})", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = contrast,
                    onValueChange = { contrast = it; applyFilters() },
                    valueRange = 0.4f..2.2f,
                    colors = SliderDefaults.colors(thumbColor = AccentYellow, activeTrackColor = AccentYellow)
                )

                // Saturation
                Text("Saturation (${"%.1f".format(saturation)})", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it; applyFilters() },
                    valueRange = 0f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = AccentYellow, activeTrackColor = AccentYellow)
                )

                // Warmth
                Text("Warmth / Tint (${warmth.toInt()})", color = TextSecondary, fontSize = 13.sp)
                Slider(
                    value = warmth,
                    onValueChange = { warmth = it; applyFilters() },
                    valueRange = -40f..40f,
                    colors = SliderDefaults.colors(thumbColor = AccentYellow, activeTrackColor = AccentYellow)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val bmp = filteredBitmap ?: return@Button
                            scope.launch {
                                val uri = MediaStoreShareHelper.saveBitmapToGallery(context, bmp, "Adjusted")
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
                            brightness = 0f
                            contrast = 1f
                            saturation = 1f
                            warmth = 0f
                            filteredBitmap = sourceBitmap
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Text("Reset All", color = TextSecondary)
                    }
                }
            }
        }
    }
}
