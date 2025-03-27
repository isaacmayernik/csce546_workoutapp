package com.example.workoutapp546

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat

@Composable
fun RequestNotificationPermission(
    onPermissionResult: (Boolean) -> Unit = {},
    showInitialDialog: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val notificationService = remember { NotificationService(context) }
    val showDialog = remember { mutableStateOf(showInitialDialog) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { onPermissionResult(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
                onDismiss()
            },
            title = { Text("Enable Notifications") },
            text = {
                Column {
                    Text("Would you like to receive daily motivational messages?")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Text("\nYou'll need to enable notifications in system settings.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        } else {
                            Intent(Settings.ACTION_SETTINGS)
                        }
                        notificationLauncher.launch(intent)
                    }
                ) { Text("Enable") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        onDismiss()
                    }
                ) { Text("Not Now") }
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!notificationService.wasNotificationRequested()) {
            showDialog.value = true
            notificationService.setNotificationsRequested(true)
        }
    }
}