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
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.engine.PrivacyBrushEngine
import com.rvbhati.phototool.engine.PrivacyMode
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyBlurScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var privacyMode by remember { mutableStateOf(PrivacyMode.PIXELATE) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                sourceBitmap = bmp
                currentBitmap = bmp
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Privacy Blur & Pixelate", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                        colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
                    ) {
                        Icon(Icons.Rounded.VisibilityOff, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo to Censor", color = OledBlack, fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Censor Style", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = privacyMode == PrivacyMode.PIXELATE,
                        onClick = { privacyMode = PrivacyMode.PIXELATE },
                        label = { Text("Pixelate / Mosaic") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentYellow),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = privacyMode == PrivacyMode.BLUR,
                        onClick = { privacyMode = PrivacyMode.BLUR },
                        label = { Text("Gaussian Blur") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentYellow),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = privacyMode == PrivacyMode.BLACKOUT,
                        onClick = { privacyMode = PrivacyMode.BLACKOUT },
                        label = { Text("Blackout") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentYellow),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Quick Censor Areas", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isProcessing = true
                            scope.launch {
                                // Censor lower third (e.g. Aadhaar / card number)
                                val w = bmp.width
                                val h = bmp.height
                                val targetRect = Rect(0, (h * 0.65f).toInt(), w, (h * 0.92f).toInt())
                                currentBitmap = PrivacyBrushEngine.applyPrivacyToRect(bmp, targetRect, privacyMode)
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Hide Bottom/ID No", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            isProcessing = true
                            scope.launch {
                                // Censor center (e.g. face / qr code)
                                val w = bmp.width
                                val h = bmp.height
                                val targetRect = Rect((w * 0.25f).toInt(), (h * 0.2f).toInt(), (w * 0.75f).toInt(), (h * 0.65f).toInt())
                                currentBitmap = PrivacyBrushEngine.applyPrivacyToRect(bmp, targetRect, privacyMode)
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Hide Center/Face", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val uri = MediaStoreShareHelper.saveBitmapToGallery(context, bmp, "Privacy_Protected")
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
