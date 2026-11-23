package com.rvbhati.phototool.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.rvbhati.phototool.engine.OcrEngine
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedText by remember { mutableStateOf("") }
    var useDevanagari by remember { mutableStateOf(true) }
    var isExtracting by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                sourceBitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri)
                extractedText = ""
            }
        }
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Offline OCR (Text Extractor)", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            if (sourceBitmap == null) {
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
                        Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                        ) {
                            Text("Select Document / Note Photo", color = OledBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sourceBitmap!!.asImageBitmap(),
                        contentDescription = "Document",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Devanagari (Hindi) + Latin Script", color = TextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = useDevanagari,
                        onCheckedChange = { useDevanagari = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentPurple)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val bmp = sourceBitmap ?: return@Button
                        isExtracting = true
                        scope.launch {
                            extractedText = OcrEngine.extractText(bmp, isDevanagari = useDevanagari)
                            isExtracting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isExtracting,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    if (isExtracting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OledBlack)
                    } else {
                        Icon(Icons.Rounded.TextSnippet, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extract Text Offline", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                }

                if (extractedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Extracted Text Result:", color = AccentPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = extractedText,
                        onValueChange = { extractedText = it },
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Extracted Text", extractedText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, extractedText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Extracted Text"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, tint = OledBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", color = OledBlack)
                        }
                    }
                }
            }
        }
    }
}
