package com.rvbhati.phototool.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvbhati.phototool.AppState
import com.rvbhati.phototool.engine.BitmapUtils
import com.rvbhati.phototool.engine.InAppUpdateEngine
import com.rvbhati.phototool.engine.UpdateInfo
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isDark = AppState.isDarkMode
    val isHi = AppState.isHindi

    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF1F5F9)
    val cardColor = if (isDark) Color(0xFF121418) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0x26FFFFFF) else Color(0x1F000000)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var freedCacheMb by remember { mutableStateOf<Double?>(null) }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor.copy(alpha = 0.92f),
                    titleContentColor = textPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = if (isHi) "युवा शक्ति ग्रुप" else "Yuva Shakti Group",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = if (isHi) "भाटी परिवार कुचेरा (रेवंतराम भाटी)" else "Bhati Parivar Kuchera (Revant Ram Bhati)",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                },
                actions = {
                    // Small circular Day / Night theme toggle button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                            .border(1.dp, borderColor, CircleShape)
                            .clickable {
                                AppState.isDarkMode = !AppState.isDarkMode
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Day / Night Theme",
                            tint = if (isDark) Color(0xFFFACC15) else Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Share APK button
                    IconButton(onClick = { InAppUpdateEngine.shareAppApk(context) }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = textPrimary)
                    }

                    // Bell / Notification icon with badge 3
                    Box(modifier = Modifier.padding(end = 6.dp)) {
                        IconButton(onClick = {
                            Toast.makeText(context, if (isHi) "सभी सिस्टम और ऑफलाइन इंजन सक्रिय हैं" else "All offline engines are active", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Rounded.Notifications, contentDescription = "Notification", tint = textPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. डेवलपर ऑप्शन एवं अपडेट Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Tune, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHi) "डेवलपर ऑप्शन एवं अपडेट" else "Developer Options & Updates",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        // Version badge on top right
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "APK ${InAppUpdateEngine.CURRENT_VERSION}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = borderColor)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHi) "मुख्य डेवलपर व संचालक:" else "Lead Developer:", color = textSecondary, fontSize = 13.sp)
                        Text("रेवंतराम भाटी", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Developer Email
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:revantrambhati@gmail.com")
                                }
                                context.startActivity(intent)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHi) "डेवलपर ईमेल (Gmail):" else "Developer Email:", color = textSecondary, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Mail, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "revantrambhati@gmail.com",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Developer Mobile Number
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918696663615"))
                                context.startActivity(intent)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHi) "डेवलपर मोबाइल नंबर:" else "Developer Mobile:", color = textSecondary, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "+91 8696663615",
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // APK Version Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHi) "एपीके संस्करण:" else "APK Version:", color = textSecondary, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E3A8A)
                        ) {
                            Text(
                                text = InAppUpdateEngine.CURRENT_VERSION,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHi) "सिस्टम इंजन:" else "System Engine:", color = textSecondary, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isHi) "100% सुरक्षित एवं ऑफलाइन सक्रिय" else "100% Secure & Offline Active",
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // In-App Update Button
                    Button(
                        onClick = {
                            isCheckingUpdate = true
                            scope.launch {
                                val info = InAppUpdateEngine.checkLatestRelease()
                                updateInfo = info
                                isCheckingUpdate = false

                                if (info.downloadUrl != null) {
                                    isDownloadingUpdate = true
                                    val apk = InAppUpdateEngine.downloadApkFile(context, info.downloadUrl) { progress ->
                                        downloadProgress = progress
                                    }
                                    isDownloadingUpdate = false
                                    downloadedApkFile = apk

                                    if (apk != null) {
                                        Toast.makeText(context, if (isHi) "अपडेट डाउनलोड हो गया! इंस्टॉल शुरू हो रहा है..." else "Update downloaded! Starting install...", Toast.LENGTH_SHORT).show()
                                        InAppUpdateEngine.installApk(context, apk)
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isCheckingUpdate && !isDownloadingUpdate,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isHi) "अपडेट की जांच हो रही है..." else "Checking for updates...")
                        } else if (isDownloadingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isHi) "डाउनलोड हो रहा है: ${(downloadProgress * 100).toInt()}%" else "Downloading: ${(downloadProgress * 100).toInt()}%")
                        } else {
                            Icon(Icons.Rounded.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHi) "अपडेट की जांच करें (Check for Updates)" else "Check for Updates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    if (isDownloadingUpdate) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4ADE80)
                        )
                    }

                    downloadedApkFile?.let { file ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { InAppUpdateEngine.installApk(context, file) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHi) "1-क्लिक में अभी इंस्टॉल करें (Install APK)" else "Install APK Now (1-Click)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. भाषा चयन (Language Toggle)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        AppState.isHindi = !AppState.isHindi
                        Toast.makeText(
                            context,
                            if (AppState.isHindi) "भाषा बदलकर हिन्दी कर दी गई है" else "Language switched to English",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Translate, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isHi) "भाषा चयन" else "Language Selection", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(
                                if (isHi) "सक्रिय भाषा: हिन्दी (बदलने हेतु टैप करें)" else "Active: English (Tap to change)",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = if (isHi) "हिन्दी >" else "English >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 3. Cache & Storage Cleaner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF059669).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.CleaningServices, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isHi) "कैश व स्टोरेज क्लीनर" else "Cache & Storage Cleaner", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(if (isHi) "अस्थायी फाइलें साफ कर फोन स्पेस खाली करें" else "Free device storage by cleaning temporary files", fontSize = 11.sp, color = textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val bytes = BitmapUtils.clearCache(context)
                            val mb = bytes / (1024.0 * 1024.0)
                            freedCacheMb = mb
                            Toast.makeText(
                                context,
                                if (isHi) "सफलतापूर्वक ${"%.2f".format(mb)} MB कैश साफ किया गया!" else "Successfully cleaned ${"%.2f".format(mb)} MB cache!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isHi) "कैश साफ करें (Clean Cache)" else "Clean Cache Now", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    freedCacheMb?.let { mb ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✓ Freed ${"%.2f".format(mb)} MB space", color = Color(0xFF34D399), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
