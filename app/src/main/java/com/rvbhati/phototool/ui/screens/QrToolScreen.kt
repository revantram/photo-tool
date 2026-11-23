package com.rvbhati.phototool.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.rvbhati.phototool.engine.QrCodeEngine
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrToolScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Generate, 1: Scan
    var inputText by remember { mutableStateOf("https://github.com/revantram/photo-tool") }
    var generatedQr by remember { mutableStateOf<Bitmap?>(null) }
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isScanning = true
            scope.launch {
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                if (bmp != null) {
                    scannedResult = QrCodeEngine.scanBarcodeFromBitmap(bmp) ?: "No QR or Barcode detected in image."
                } else {
                    scannedResult = "Failed to load image."
                }
                isScanning = false
            }
        }
    }

    LaunchedEffect(Unit) {
        generatedQr = QrCodeEngine.generateQrBitmap(inputText)
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("QR Code & Barcode Studio", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceElevated,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Generate QR", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Scan from Image", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        scope.launch {
                            if (it.isNotBlank()) {
                                generatedQr = QrCodeEngine.generateQrBitmap(it)
                            }
                        }
                    },
                    label = { Text("Enter Text, URL or UPI ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                generatedQr?.let { qr ->
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(androidx.compose.ui.graphics.Color.White)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qr.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val uri = MediaStoreShareHelper.saveBitmapToGallery(context, qr, "QRCode")
                                    if (uri != null) {
                                        Toast.makeText(context, "Saved QR to Pictures/PhotoTool!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Save QR Code", color = OledBlack, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val uri = MediaStoreShareHelper.saveBitmapToGallery(context, qr, "QRCode")
                                    if (uri != null) {
                                        MediaStoreShareHelper.shareImageUri(context, uri)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Text("Share QR")
                        }
                    }
                }
            } else {
                // Scan tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Select Image with QR / Barcode")
                        }
                    }
                }

                if (isScanning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = PrimaryBlue)
                }

                scannedResult?.let { res ->
                    Spacer(modifier = Modifier.height(18.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Scanned Content:", color = AccentCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(res, color = TextPrimary, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Scanned QR", res)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Result")
                            }
                        }
                    }
                }
            }
        }
    }
}
