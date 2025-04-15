package com.example.workoutapp546.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import com.example.workoutapp546.SharedViewModel
import com.example.workoutapp546.notifications.NotificationScheduler

@Composable
fun Settings(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
//    var nextNotificationTime by remember { mutableStateOf("Calculating...") }

    var isDarkMode by remember { mutableStateOf(sharedViewModel.isDarkMode) }

//    LaunchedEffect(Unit) {
//        NotificationScheduler.getNextScheduledTime(context) { time ->
//            nextNotificationTime = time
//        }
//    }

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
    }

    fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent)
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Open notifications settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { openNotificationSettings() },
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }

//            // Next notification time for TESTING
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//                    .padding(horizontal = 8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = nextNotificationTime,
//                    style = MaterialTheme.typography.titleMedium.copy(
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Medium
//                    ),
//                    modifier = Modifier.weight(1f)
//                )
//            }
        }
    }
}