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
import com.rvbhati.phototool.engine.CardMergeLayout
import com.rvbhati.phototool.engine.IdCardMergerEngine
import com.rvbhati.phototool.engine.ImageToPdfEngine
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCardMergerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var backBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedLayout by remember { mutableStateOf(CardMergeLayout.VERTICAL_STACK) }
    var selfAttestedText by remember { mutableStateOf("Self Attested") }
    var includeAttestation by remember { mutableStateOf(false) }
    var mergedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var pickingForFront by remember { mutableStateOf(true) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                if (pickingForFront) {
                    frontBitmap = bmp
                } else {
                    backBitmap = bmp
                }
                mergedBitmap = null
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("ID Card Merger & A4 Sheet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            // Dual Slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Front Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, if (frontBitmap != null) PrimaryBlue else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            pickingForFront = true
                            pickImageLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (frontBitmap != null) {
                        Image(
                            bitmap = frontBitmap!!.asImageBitmap(),
                            contentDescription = "Front Card",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+ Front Side", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Back Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, if (backBitmap != null) AccentCyan else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            pickingForFront = false
                            pickImageLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (backBitmap != null) {
                        Image(
                            bitmap = backBitmap!!.asImageBitmap(),
                            contentDescription = "Back Card",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+ Back Side", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Layout Mode Selector
            Text("Select Merge Layout", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardMergeLayout.values().forEach { layout ->
                    val isSelected = selectedLayout == layout
                    val label = when (layout) {
                        CardMergeLayout.VERTICAL_STACK -> "Top-Bottom"
                        CardMergeLayout.HORIZONTAL_SIDE_BY_SIDE -> "Side-by-Side"
                        CardMergeLayout.A4_PRINTABLE_SHEET -> "A4 Sheet"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedLayout = layout
                            mergedBitmap = null
                        },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Self Attested Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeAttestation,
                    onCheckedChange = { includeAttestation = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                )
                Text("Add 'Self Attested' watermark text", color = TextPrimary, fontSize = 14.sp)
            }

            if (includeAttestation) {
                OutlinedTextField(
                    value = selfAttestedText,
                    onValueChange = { selfAttestedText = it },
                    label = { Text("Watermark text") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val front = frontBitmap
                    val back = backBitmap
                    if (front == null || back == null) {
                        Toast.makeText(context, "Please select both Front and Back sides", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    scope.launch {
                        val attestation = if (includeAttestation) selfAttestedText else null
                        mergedBitmap = IdCardMergerEngine.mergeIdCard(
                            frontBitmap = front,
                            backBitmap = back,
                            layout = selectedLayout,
                            selfAttestedText = attestation
                        )
                        isProcessing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = frontBitmap != null && backBitmap != null && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextPrimary)
                } else {
                    Icon(Icons.Rounded.CallMerge, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Merge ID Cards Now", fontWeight = FontWeight.Bold)
                }
            }

            // Merged Preview
            mergedBitmap?.let { merged ->
                Spacer(modifier = Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Merged Preview (${merged.width} × ${merged.height} px)", color = AccentCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Image(
                            bitmap = merged.asImageBitmap(),
                            contentDescription = "Merged Result",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val uri = MediaStoreShareHelper.saveBitmapToGallery(context, merged, "IDCard_Merged")
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
                            Button(
                                onClick = {
                                    scope.launch {
                                        val pdfFile = File(context.cacheDir, "ID_Card_${System.currentTimeMillis()}.pdf")
                                        ImageToPdfEngine.createPdfFromBitmaps(listOf(merged), pdfFile)
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
