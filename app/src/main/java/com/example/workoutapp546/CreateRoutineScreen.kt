package com.example.workoutapp546

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun CreateRoutine(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val workoutNames = workoutMuscleMap.keys.toList()
    val selectedWorkouts = remember { mutableStateListOf<RoutineWorkout>() }
    var routineName by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    var isNavigatingAway by remember { mutableStateOf(false) }

    LaunchedEffect(selectedWorkouts) {
        if (isNavigatingAway) {
            showConfirmationDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Name routine
        BasicTextField(
            value = routineName,
            onValueChange = { routineName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (routineName.isEmpty()) {
                        Text("Enter routine name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
        )

        // List of workouts
        LazyColumn {
            items(workoutNames) { workoutName ->
                val sets = selectedWorkouts.find { it.name == workoutName }?.sets ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workoutName,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { if (sets > 0) selectedWorkouts.removeAll { it.name == workoutName } }
                    ) {
                        Text("-")
                    }
                    Text(
                        "$sets sets",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Button(
                        onClick = {
                            val existing = selectedWorkouts.find { it.name == workoutName }
                            if (existing != null) {
                                selectedWorkouts[selectedWorkouts.indexOf(existing)] = existing.copy(sets = existing.sets + 1)
                            } else {
                                selectedWorkouts.add(RoutineWorkout(workoutName, 1))
                            }
                        }
                    ) {
                        Text("+")
                    }
                }
            }
        }

        // Save routine
        Button(
            onClick = {
                if (routineName.isNotEmpty() && selectedWorkouts.isNotEmpty()) {
                    sharedViewModel.addRoutine(sharedPreferences, Routine(routineName, selectedWorkouts))
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save Routine")
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        isNavigatingAway = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.route != "create_routine") {
                isNavigatingAway = true
            }
        }
    }
}