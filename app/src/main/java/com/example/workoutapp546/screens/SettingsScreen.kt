package com.example.workoutapp546.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.workoutapp546.NotificationService
import com.example.workoutapp546.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun Settings(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val notificationService = remember { NotificationService(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    var isDarkMode by remember { mutableStateOf(sharedViewModel.isDarkMode) }
    var notificationsEnabled by remember { mutableStateOf(notificationService.areNotificationsEnabled()) }
    var nextNotificationTime by remember { mutableStateOf("Calculating...") }

    // launcher for opening notifications settings
    // also handles result -- if user does not enable in system settings, display a snackbar
    //      otherwise, correctly change state of the notification switch
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // update state when returning from system settings
        val newState = notificationService.areNotificationsEnabled()
        notificationsEnabled = newState
        notificationService.setNotificationPreference(newState)

        // show warning
        if (!newState) {
            snackbarMessage = "Notifications not enabled in system settings"
            showSnackbar = true
        }
    }

    fun updateNotificationTime() {
        val nextAlarmTime = notificationService.getNextAlarmTime()
        if (nextAlarmTime > 0) {
            val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            nextNotificationTime = dateFormat.format(nextAlarmTime)
        } else {
            nextNotificationTime = "Not scheduled"
        }
    }

    LaunchedEffect(sharedViewModel.isDarkMode) {
        isDarkMode = sharedViewModel.isDarkMode
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(snackbarMessage)
            showSnackbar = false
        }
    }

    LaunchedEffect(Unit) {
        if (sharedViewModel.routineJustCreated) {
            snackbarHostState.showSnackbar("Routine created successfully!")
            sharedViewModel.resetRoutineCreated()
        }
        updateNotificationTime()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dark mode setting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        isDarkMode = it
                        sharedViewModel.toggleDarkMode(it, sharedPreferences)
                    },
                    modifier = Modifier.wrapContentSize()
                )
            }

            // Create routine
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Create a routine",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { navController.navigate("create_routine") },
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = "Create",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        )
                    )
                }
            }

            // Notifications switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enable Notifications",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // API 26+, open system settings notifications
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            notificationLauncher.launch(intent)
                        } else {
                            // if API <= 25, toggle directly
                            notificationService.setNotificationPreference(enabled)
                            notificationsEnabled = enabled
                        }
                    },
                    modifier = Modifier.wrapContentSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Next notification: $nextNotificationTime",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

}