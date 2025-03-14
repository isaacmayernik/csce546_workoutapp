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

    LaunchedEffect(selectedWorkouts) {
        sharedViewModel.setUnsavedChanges(selectedWorkouts.isNotEmpty())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                            Text(
                                "Enter routine name",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                },
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            // List of workouts
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
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
                            onClick = {
                                if (sets > 0) {
                                    selectedWorkouts.removeAll { it.name == workoutName }
                                    sharedViewModel.setUnsavedChanges(selectedWorkouts.isNotEmpty())
                                }
                            }
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
                                    selectedWorkouts[selectedWorkouts.indexOf(existing)] =
                                        existing.copy(sets = existing.sets + 1)
                                } else {
                                    selectedWorkouts.add(RoutineWorkout(workoutName, 1))
                                }
                                sharedViewModel.setUnsavedChanges(selectedWorkouts.isNotEmpty())
                            }
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        }

        // Save routine
        Button(
            onClick = {
                if (routineName.isNotEmpty() && selectedWorkouts.isNotEmpty()) {
                    sharedViewModel.addRoutine(
                        sharedPreferences,
                        Routine(routineName, selectedWorkouts)
                    )
                    sharedViewModel.setUnsavedChanges(false)
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save Routine")
        }
    }
}