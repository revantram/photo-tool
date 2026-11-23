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
import com.rvbhati.phototool.engine.MediaStoreShareHelper
import com.rvbhati.phototool.engine.ZipArchiveEngine
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipPackScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedUris = remember { mutableStateListOf<Uri>() }
    var isArchiving by remember { mutableStateOf(false) }
    var generatedZipFile by remember { mutableStateOf<File?>(null) }

    val pickMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris.clear()
        selectedUris.addAll(uris)
        generatedZipFile = null
    }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("ZIP Archive Creator", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Rounded.Archive, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Images for ZIP Archive (${selectedUris.size} selected)", fontWeight = FontWeight.Bold)
            }

            if (selectedUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("${selectedUris.size} images ready to bundle", color = AccentGreen, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isArchiving = true
                        scope.launch {
                            val tempDir = File(context.cacheDir, "zip_staging_${System.currentTimeMillis()}").apply { mkdirs() }
                            val stagingFiles = mutableListOf<File>()

                            selectedUris.forEachIndexed { i, uri ->
                                val file = File(tempDir, "photo_${i + 1}.jpg")
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                if (file.exists()) {
                                    stagingFiles.add(file)
                                }
                            }

                            val zipOut = File(context.cacheDir, "PhotoTool_Pack_${System.currentTimeMillis()}.zip")
                            val success = ZipArchiveEngine.createZipArchive(stagingFiles, zipOut)
                            if (success) {
                                generatedZipFile = zipOut
                            }
                            isArchiving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isArchiving,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    if (isArchiving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OledBlack)
                    } else {
                        Icon(Icons.Rounded.FolderZip, contentDescription = null, tint = OledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create ZIP File Now", color = OledBlack, fontWeight = FontWeight.Bold)
                    }
                }

                generatedZipFile?.let { zip ->
                    Spacer(modifier = Modifier.height(18.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ZIP Created Successfully!", color = AccentGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Size: ${"%.1f".format(zip.length() / 1024.0)} KB", color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    MediaStoreShareHelper.shareFile(context, zip, "application/zip")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share ZIP Archive")
                            }
                        }
                    }
                }
            }
        }
    }
}
