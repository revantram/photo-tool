package com.rvbhati.phototool.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rvbhati.phototool.engine.BitmapUtils
import com.rvbhati.phototool.engine.InAppUpdateEngine
import com.rvbhati.phototool.engine.UpdateInfo
import com.rvbhati.phototool.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

data class OtherDeveloperApp(
    val name: String,
    val description: String,
    val githubUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showOtherApps by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var freedCacheMb by remember { mutableStateOf<Double?>(null) }

    val otherApps = listOf(
        OtherDeveloperApp(
            name = "RV Bhati Player",
            description = "शक्तिशाली मीडिया प्लेयर व ऑडियो-वीडियो प्लेयर",
            githubUrl = "https://github.com/revantram/rv-bhati-player"
        ),
        OtherDeveloperApp(
            name = "मेरी तैयारी (Meri Taiyari)",
            description = "प्रतियोगी परीक्षा MCQ टेस्ट व अध्ययन ऐप",
            githubUrl = "https://github.com/revantram/meri-taiyari-mcq-prep"
        ),
        OtherDeveloperApp(
            name = "युवा शक्ति ऐप (Yuva Shakti App)",
            description = "भाटी परिवार कुचेरा आधिकारिक कम्युनिटी ऐप",
            githubUrl = "https://github.com/revantram/yuva-shakti-app"
        ),
        OtherDeveloperApp(
            name = "हाजिरी ऐप (Hajiri App)",
            description = "iOS Liquid Glass UI • मोबाइल लॉगिन • सैलरी कैलकुलेटर",
            githubUrl = "https://github.com/revantram/hajiri-app"
        ),
        OtherDeveloperApp(
            name = "TV Launcher",
            description = "Android TV व Fire TV Stick कस्टम लॉन्चर",
            githubUrl = "https://github.com/revantram/TV-launcher"
        ),
        OtherDeveloperApp(
            name = "उत्कर्ष MCQ (Utkarsh MCQ)",
            description = "MCQ क्विज़ व प्रश्नोत्तरी अभ्यास ऐप",
            githubUrl = "https://github.com/revantram/Utkarsh-"
        )
    )

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OledBlack,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "युवा शक्ति ग्रुप",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "भाटी परिवार कुचेरा (रेवंतराम भाटी)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { InAppUpdateEngine.shareAppApk(context) }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = Color.White)
                    }
                    Box(modifier = Modifier.padding(end = 6.dp)) {
                        IconButton(onClick = {
                            Toast.makeText(context, "सभी सिस्टम और ऑफलाइन इंजन सक्रिय हैं", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Rounded.Notifications, contentDescription = "Notification", tint = Color.White)
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
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
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
                                text = "डेवलपर ऑप्शन एवं अपडेट",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Version badge on top right
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "APK ${InAppUpdateEngine.CURRENT_VERSION}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0x1AFFFFFF))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("मुख्य डेवलपर व संचालक:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("रेवंतराम भाटी", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        Text("डेवलपर ईमेल (Gmail):", color = Color(0xFF94A3B8), fontSize = 13.sp)
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
                        Text("डेवलपर मोबाइल नंबर:", color = Color(0xFF94A3B8), fontSize = 13.sp)
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

                    // APK Version Pill (Without build code as explicitly requested)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("एपीके संस्करण (APK Version):", color = Color(0xFF94A3B8), fontSize = 13.sp)
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
                        Text("क्लाउड / इंजन:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "100% सुरक्षित एवं ऑफलाइन सक्रिय",
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

                                // Automatically start download if URL exists
                                if (info.downloadUrl != null) {
                                    isDownloadingUpdate = true
                                    val apk = InAppUpdateEngine.downloadApkFile(context, info.downloadUrl) { progress ->
                                        downloadProgress = progress
                                    }
                                    isDownloadingUpdate = false
                                    downloadedApkFile = apk

                                    if (apk != null) {
                                        Toast.makeText(context, "अपडेट डाउनलोड हो गया! इंस्टॉल शुरू हो रहा है...", Toast.LENGTH_SHORT).show()
                                        InAppUpdateEngine.installApk(context, apk)
                                    } else {
                                        Toast.makeText(context, "सीधा डाउनलोड लिंक खोला जा रहा है...", Toast.LENGTH_SHORT).show()
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
                            Text("अपडेट की जांच हो रही है...")
                        } else if (isDownloadingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("डाउनलोड हो रहा है: ${(downloadProgress * 100).toInt()}%")
                        } else {
                            Icon(Icons.Rounded.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("अपडेट की जांच करें (Check for Updates)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                            Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = OledBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1-क्लिक में अभी इंस्टॉल करें (Install APK)", color = OledBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. डेवलपर के और दूसरे एप्लीकेशन Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOtherApps = !showOtherApps }
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
                                Icon(Icons.Rounded.Widgets, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "डेवलपर के और दूसरे एप्लीकेशन",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "रेवंतराम भाटी के अन्य ऐप्स देखें व 1-क्लिक में इंस्टॉल करें",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "6+ ऐप्स >",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showOtherApps) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            otherApps.forEach { app ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF181C24),
                                    border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.githubUrl))
                                            context.startActivity(intent)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                            Text(app.description, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                            contentDescription = "Open",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. एडमिन कंट्रोल (Admin Control)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "एडमिन मोड सक्रिय: रेवंतराम भाटी", Toast.LENGTH_SHORT).show()
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
                                .background(Color(0xFFD97706).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("एडमिन कंट्रोल (Admin Control)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("एडमिन मोड सक्रिय • कंट्रोल सेंटर खोलें", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF14532D)
                    ) {
                        Text(
                            text = "👑 सक्रिय (Active) >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 4. ऐप शेयर करें (APK Send)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { InAppUpdateEngine.shareAppApk(context) }
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
                                .background(Color(0xFFEA580C).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null, tint = Color(0xFFFB923C), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ऐप शेयर करें (APK Send)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("व्हाट्सएप पर सीधी ऐप (APK फाइल) भेजें", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF431407)
                    ) {
                        Text(
                            text = "APK शेयर >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFB923C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 5. व्हाट्सएप ग्रुप
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918696663615"))
                        context.startActivity(intent)
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
                                .background(Color(0xFF16A34A).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Chat, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("व्हाट्सएप ग्रुप", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("भाटी परिवार आधिकारिक ग्रुप से जुड़ें", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF14532D)
                    ) {
                        Text(
                            text = "व्हाट्सएप >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 6. भाषा (Language)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
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
                            Text("भाषा (Language)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("सक्रिय भाषा: हिन्दी", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "हिन्दी >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 7. ऐप थीम (रंग-रूप)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
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
                                .background(Color(0xFFE11D48).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ऐप थीम (रंग-रूप)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("क्लासिक सफेद व साफ रूप / डार्क मोड", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF4C0519)
                    ) {
                        Text(
                            text = "OLED डार्क >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFB7185),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 8. Cache & Storage Cleaner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
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
                            Text("कैश व स्टोरेज क्लीनर", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("अस्थायी फाइलें साफ कर फोन स्पेस खाली करें", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val bytes = BitmapUtils.clearCache(context)
                            val mb = bytes / (1024.0 * 1024.0)
                            freedCacheMb = mb
                            Toast.makeText(context, "सफलतापूर्वक ${"%.2f".format(mb)} MB कैश साफ किया गया!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("कैश साफ करें (Clean Cache)", color = OledBlack, fontWeight = FontWeight.Bold)
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
