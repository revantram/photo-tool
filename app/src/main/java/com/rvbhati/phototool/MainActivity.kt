package com.rvbhati.phototool

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.navigation.compose.rememberNavController
import com.rvbhati.phototool.engine.BitmapUtils
import com.rvbhati.phototool.ui.navigation.PhotoToolNavHost
import com.rvbhati.phototool.ui.navigation.Screen
import com.rvbhati.phototool.ui.theme.PhotoToolTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhotoToolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Gallery Launcher
                    val galleryLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            navController.navigate(Screen.CropResize.route)
                        }
                    }

                    // Camera Launcher
                    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
                    val cameraLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.TakePicture()
                    ) { success: Boolean ->
                        if (success && tempCameraUri != null) {
                            navController.navigate(Screen.CropResize.route)
                        }
                    }

                    LaunchedEffect(intent) {
                        handleIncomingIntent(intent) { route ->
                            navController.navigate(route)
                        }
                    }

                    PhotoToolNavHost(
                        navController = navController,
                        onSelectGallery = {
                            galleryLauncher.launch("image/*")
                        },
                        onOpenCamera = {
                            try {
                                val tempFile = BitmapUtils.createTempImageFile(this@MainActivity)
                                val uri = FileProvider.getUriForFile(
                                    this@MainActivity,
                                    "${packageName}.fileprovider",
                                    tempFile
                                )
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Failed to launch camera: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?, onNavigate: (String) -> Unit) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        if ((Intent.ACTION_SEND == action || Intent.ACTION_SEND_MULTIPLE == action) && type?.startsWith("image/") == true) {
            onNavigate(Screen.CropResize.route)
        }
    }
}
