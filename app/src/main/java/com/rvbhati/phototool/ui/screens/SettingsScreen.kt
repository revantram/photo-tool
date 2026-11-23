package com.rvbhati.phototool.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.rvbhati.phototool.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var freedCacheMb by remember { mutableStateOf<Double?>(null) }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Storage", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            // App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Photo Tool Pro", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Version 1.0 (Build 1) • 100% Offline Studio", fontSize = 12.sp, color = AccentCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Zero paid APIs • Zero tracking • MediaStore scoped storage compliant.", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Storage & Cache Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null, tint = AccentGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cache & Storage Cleaner", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Clear intermediate cached bitmaps, temporary PDF renders, and working files to free phone space.", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val bytesFreed = BitmapUtils.clearCache(context)
                            val mb = bytesFreed / (1024.0 * 1024.0)
                            freedCacheMb = mb
                            Toast.makeText(context, "Cleaned ${"%.2f".format(mb)} MB cache", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Clean Cache Now", color = OledBlack, fontWeight = FontWeight.Bold)
                    }

                    freedCacheMb?.let { mb ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✓ Freed ${"%.2f".format(mb)} MB of temporary cache", color = AccentGreen, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Scoped Storage Location
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export Directory", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pictures/PhotoTool/", color = AccentCyan, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("All saved photos and exports appear instantly in Android Gallery and Google Photos.", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // GitHub & Open Source
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GitHub Repository", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("https://github.com/revantram/photo-tool", color = AccentCyan, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/revantram/photo-tool"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Rounded.OpenInBrowser, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View on GitHub")
                    }
                }
            }
        }
    }
}
