package com.rvbhati.phototool.ui.screens

import android.graphics.Bitmap
import android.graphics.Rect
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
import com.rvbhati.phototool.engine.ImageTransformEngine
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropResizeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resizeW by remember { mutableStateOf("1080") }
    var resizeH by remember { mutableStateOf("1080") }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                sourceBitmap = bmp
                currentBitmap = bmp
                if (bmp != null) {
                    resizeW = bmp.width.toString()
                    resizeH = bmp.height.toString()
                }
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Crop, Rotate & Resize", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            if (currentBitmap == null) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Icon(Icons.Rounded.Crop, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo to Edit", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val bmp = currentBitmap!!
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
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Dimensions: ${bmp.width} × ${bmp.height} px", color = TextSecondary, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(14.dp))

                // Transform action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                currentBitmap = ImageTransformEngine.rotateBitmap(bmp, 90f)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("90°")
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                currentBitmap = ImageTransformEngine.flipBitmap(bmp, horizontal = true)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Rounded.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Flip H")
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                currentBitmap = ImageTransformEngine.flipBitmap(bmp, horizontal = false)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Rounded.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Flip V")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aspect Ratio Crop Presets
                Text("Aspect Ratio Crop", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val ratios = listOf("1:1", "4:5", "16:9", "A4")
                    ratios.forEach { ratio ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                scope.launch {
                                    val (w, h) = bmp.width to bmp.height
                                    val rect = when (ratio) {
                                        "1:1" -> {
                                            val side = min(w, h)
                                            Rect((w - side) / 2, (h - side) / 2, (w + side) / 2, (h + side) / 2)
                                        }
                                        "4:5" -> {
                                            val targetW = min(w, (h * 4f / 5f).toInt())
                                            val targetH = (targetW * 5f / 4f).toInt()
                                            Rect((w - targetW) / 2, (h - targetH) / 2, (w + targetW) / 2, (h + targetH) / 2)
                                        }
                                        "16:9" -> {
                                            val targetH = min(h, (w * 9f / 16f).toInt())
                                            val targetW = (targetH * 16f / 9f).toInt()
                                            Rect((w - targetW) / 2, (h - targetH) / 2, (w + targetW) / 2, (h + targetH) / 2)
                                        }
                                        else -> {
                                            // A4 standard 1:1.414 ratio
                                            val targetW = min(w, (h / 1.414f).toInt())
                                            val targetH = (targetW * 1.414f).toInt()
                                            Rect((w - targetW) / 2, (h - targetH) / 2, (w + targetW) / 2, (h + targetH) / 2)
                                        }
                                    }
                                    currentBitmap = ImageTransformEngine.cropBitmap(bmp, rect)
                                }
                            },
                            label = { Text(ratio, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Resize inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = resizeW,
                        onValueChange = { resizeW = it },
                        label = { Text("Width px") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = resizeH,
                        onValueChange = { resizeH = it },
                        label = { Text("Height px") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val w = resizeW.toIntOrNull() ?: 1080
                        val h = resizeH.toIntOrNull() ?: 1080
                        scope.launch {
                            currentBitmap = ImageTransformEngine.resizeBitmap(bmp, w, h, keepAspectRatio = false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Apply Custom Pixel Resize")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val uri = MediaStoreShareHelper.saveBitmapToGallery(context, bmp, "Edited")
                                if (uri != null) {
                                    Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
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
                            sourceBitmap?.let { currentBitmap = it }
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
