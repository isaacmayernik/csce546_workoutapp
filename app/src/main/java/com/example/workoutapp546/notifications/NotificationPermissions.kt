package com.example.workoutapp546.notifications

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermissions() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            NotificationScheduler.scheduleDailyNotification(context)
        } else if (!shouldShowRationale(context)) {
            showDialog = true
        }
    }

    CheckAndRequestPermission(
        context = context,
        launcher = permissionLauncher
    )

    PermissionSettingsDialog(
        showDialog,
        onDismissClose = {
            showDialog = false
            val activity = context as Activity
            activity.finish()
        },
        onDismiss = {
            showDialog = false
        },
        context
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun CheckAndRequestPermission(
    context: Context,
    launcher: ActivityResultLauncher<String>
) {
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun PermissionSettingsDialog(
    show: Boolean,
    onDismissClose: () -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismissClose,
            title = { Text("Permission Required") },
            text = { Text("Please enable notifications in settings") },
            confirmButton = {
                TextButton(onClick = {
                    context.openAppSettings()
                    onDismiss()
                }) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissClose() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Extension function for Context
// Opens the settings menu, is called if the user clicks confirm
private fun Context.openAppSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    })
}

//Android 18(?)+ only allows for this to happen twice, if you deny twice it won't ask again
//If we need the permission for the app to work we need to do something else
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun shouldShowRationale(context: Context): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(
        context as Activity,
        POST_NOTIFICATIONS
    )
}