package com.rvbhati.phototool.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvbhati.phototool.engine.BitmapUtils
import com.rvbhati.phototool.engine.KbTargetCompressor
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchProcessScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedUris = remember { mutableStateListOf<Uri>() }
    var targetKb by remember { mutableIntStateOf(100) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressCount by remember { mutableIntStateOf(0) }
    var completedCount by remember { mutableIntStateOf(0) }

    val pickMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris.clear()
        selectedUris.addAll(uris)
        completedCount = 0
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Batch Photo Processing", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            Button(
                onClick = { pickMultipleLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Icon(Icons.Rounded.Layers, contentDescription = null, tint = OledBlack)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Multiple Photos (${selectedUris.size} chosen)", color = OledBlack, fontWeight = FontWeight.Bold)
            }

            if (selectedUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))

                Text("Target File Size: $targetKb KB per photo", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = targetKb.toFloat(),
                    onValueChange = { targetKb = it.toInt() },
                    valueRange = 20f..500f,
                    colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        progressCount = 0
                        scope.launch {
                            for ((index, uri) in selectedUris.withIndex()) {
                                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                                if (bmp != null) {
                                    val result = KbTargetCompressor.compressToMaxKb(bmp, targetKb)
                                    MediaStoreShareHelper.saveBytesToGallery(
                                        context,
                                        result.bytes,
                                        "Batch_${targetKb}KB_${index + 1}_${System.currentTimeMillis()}.jpg"
                                    )
                                    bmp.recycle()
                                }
                                progressCount = index + 1
                            }
                            completedCount = selectedUris.size
                            isProcessing = false
                            Toast.makeText(context, "All $completedCount photos compressed & saved!", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextPrimary)
                    } else {
                        Icon(Icons.Rounded.Compress, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Batch Compression", fontWeight = FontWeight.Bold)
                    }
                }

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progressCount.toFloat() / selectedUris.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Processing $progressCount of ${selectedUris.size}...", color = TextSecondary)
                }

                if (completedCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("✓ Successfully processed $completedCount photos to Pictures/PhotoTool", color = AccentGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
