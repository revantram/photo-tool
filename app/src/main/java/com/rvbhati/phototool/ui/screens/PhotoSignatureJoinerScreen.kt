package com.rvbhati.phototool.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.rvbhati.phototool.engine.JoinedPhotoSignatureResult
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.engine.PhotoSignatureJoinerEngine
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSignatureJoinerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outWidth by remember { mutableIntStateOf(200) }
    var outHeight by remember { mutableIntStateOf(250) }
    var photoRatio by remember { mutableFloatStateOf(0.72f) }
    var result by remember { mutableStateOf<JoinedPhotoSignatureResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var isSelectingPhoto by remember { mutableStateOf(true) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                if (isSelectingPhoto) {
                    photoBitmap = bmp
                } else {
                    signatureBitmap = bmp
                }
                result = null
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Photo + Sign Exam Joiner", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, if (photoBitmap != null) PrimaryBlue else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            isSelectingPhoto = true
                            pickImageLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(bitmap = photoBitmap!!.asImageBitmap(), contentDescription = "Photo", modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Portrait, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                            Text("+ Top Photo", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Signature slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, if (signatureBitmap != null) AccentOrange else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            isSelectingPhoto = false
                            pickImageLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (signatureBitmap != null) {
                        Image(bitmap = signatureBitmap!!.asImageBitmap(), contentDescription = "Signature", modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Draw, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(32.dp))
                            Text("+ Bottom Sign", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets for dimension
            Text("Output Dimensions: ${outWidth} × ${outHeight} px", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = outWidth == 200 && outHeight == 250,
                    onClick = { outWidth = 200; outHeight = 250 },
                    label = { Text("200 × 250 px") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentOrange)
                )
                FilterChip(
                    selected = outWidth == 300 && outHeight == 400,
                    onClick = { outWidth = 300; outHeight = 400 },
                    label = { Text("300 × 400 px") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentOrange)
                )
                FilterChip(
                    selected = outWidth == 350 && outHeight == 450,
                    onClick = { outWidth = 350; outHeight = 450 },
                    label = { Text("350 × 450 px") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentOrange)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Photo vs Signature Height Split: ${(photoRatio * 100).toInt()}%", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = photoRatio,
                onValueChange = { photoRatio = it },
                valueRange = 0.5f..0.85f,
                colors = SliderDefaults.colors(thumbColor = AccentOrange, activeTrackColor = AccentOrange)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val p = photoBitmap
                    val s = signatureBitmap
                    if (p == null || s == null) {
                        Toast.makeText(context, "Please select both Photo and Signature", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    scope.launch {
                        result = PhotoSignatureJoinerEngine.joinPhotoAndSignature(
                            photoBitmap = p,
                            signatureBitmap = s,
                            outputWidth = outWidth,
                            outputHeight = outHeight,
                            photoRatio = photoRatio
                        )
                        isProcessing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = photoBitmap != null && signatureBitmap != null && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OledBlack)
                } else {
                    Icon(Icons.Rounded.Portrait, contentDescription = null, tint = OledBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Photo & Signature", color = OledBlack, fontWeight = FontWeight.Bold)
                }
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Exam Joined Card (${"%.1f".format(res.sizeKb)} KB)", color = AccentOrange, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = res.bitmap.asImageBitmap(),
                            contentDescription = "Joined Image",
                            modifier = Modifier
                                .width(160.dp)
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val uri = MediaStoreShareHelper.saveBytesToGallery(context, res.jpegBytes, "Photo_Sign_${System.currentTimeMillis()}.jpg")
                                        if (uri != null) {
                                            Toast.makeText(context, "Saved to Pictures/PhotoTool!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) {
                                Text("Save Image", color = OledBlack, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val uri = MediaStoreShareHelper.saveBytesToGallery(context, res.jpegBytes, "Photo_Sign_${System.currentTimeMillis()}.jpg")
                                        if (uri != null) {
                                            MediaStoreShareHelper.shareImageUri(context, uri)
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
}
