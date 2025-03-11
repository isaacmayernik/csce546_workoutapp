package com.example.workoutapp546

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

@Composable
fun Settings(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var isDarkMode by remember { mutableStateOf(sharedViewModel.isDarkMode) }

    LaunchedEffect(sharedViewModel.isDarkMode) {
        isDarkMode = sharedViewModel.isDarkMode
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Dark mode setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
                .padding(vertical = 12.dp),
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
    }
}