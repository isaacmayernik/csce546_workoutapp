package com.example.workoutapp546.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.workoutapp546.SharedViewModel


@Composable
fun WorkoutHistory(
    sharedViewModel: SharedViewModel,
    navController: NavHostController,
    workouts: List<Workout>,
    onDeleteWorkout: (Workout) -> Unit,
    onUpdateReps: (Workout, List<WorkoutSet>) -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }
    var showRepsDialog by remember { mutableStateOf(false) }
    var workoutToUpdate by remember { mutableStateOf<Workout?>(null) }

    Scaffold(
        topBar = {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text("Workout History", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (workouts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No workouts recorded for today")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(workouts) { workout ->
                        val isNewPR by remember(workout) {
                            mutableStateOf(
                                sharedViewModel.personalRecords[workout.name]?.let { pr ->
                                    (workout.maxReps * (workout.weight ?: 1f)).toInt() >= pr
                                } == true
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row (
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(workout.name, style = MaterialTheme.typography.titleMedium)
                                    if (isNewPR) {
                                        Text("New PR!", color = Color.Green, style = MaterialTheme.typography.labelLarge)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                workout.sets.forEachIndexed { index, set ->
                                    if (index == 0 || set.reps > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Set ${index + 1}: ${set.reps} reps")
                                            Spacer(modifier = Modifier.width(4.dp))

                                            set.weight?.let { weight ->
                                                Text(" @ ${weight}kg", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            workoutToUpdate = workout
                                            showRepsDialog = true
                                        }
                                    ) { Text("Edit Reps") }

                                    Button(
                                        onClick = {
                                            workoutToDelete = workout
                                            showDeleteConfirmation = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Workout") },
            text = { Text("Are you sure you want to delete this workout?") },
            confirmButton = {
                Button(
                    onClick = {
                        workoutToDelete?.let { onDeleteWorkout(it) }
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRepsDialog && workoutToUpdate != null) {
        val setsList = remember {
            mutableStateListOf<WorkoutSet>().apply {
                add(workoutToUpdate!!.sets.firstOrNull() ?: WorkoutSet(0))
            }
        }

        AlertDialog(
            onDismissRequest = { showRepsDialog = false },
            title = { Text(
                "Edit reps for ${workoutToUpdate!!.name}",
                style = MaterialTheme.typography.titleMedium
            ) },
            text = {
                Column {
                    setsList.forEachIndexed { index, set ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text("Set ${index + 1}: ", modifier = Modifier.width(60.dp))

                            // Reps input
                            OutlinedTextField(
                                value = if (set.reps == 0) "# of reps" else set.reps.toString(),
                                onValueChange = { newValue ->
                                    if (newValue.toIntOrNull() != null) {
                                        setsList[index] = set.copy(reps = newValue.toInt())
                                    }
                                },
                                modifier = Modifier.width(90.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = if (set.reps == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Weight input
                            OutlinedTextField(
                                value = set.weight?.toString() ?: "",
                                onValueChange = { newValue ->
                                    setsList[index] = set.copy(
                                        weight = newValue.toFloatOrNull()
                                    )
                                },
                                label = { Text("Weight") },
                                modifier = Modifier.width(90.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = if (set.weight == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true
                            )

                            IconButton(
                                onClick = { setsList.removeAt(index) },
                                enabled = setsList.size > 1
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove set")
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (setsList.size < workoutToUpdate!!.sets.size) {
                                setsList.add(
                                    WorkoutSet(
                                        reps = setsList.lastOrNull()?.reps ?: 0,
                                        weight = setsList.lastOrNull()?.weight
                                    )
                                )
                            }
                        },
                        enabled = setsList.size < workoutToUpdate!!.sets.size,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Set")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Fill remaining sets with last entered values when saving
                        val filledSets = setsList.toMutableList().apply {
                            while (size < workoutToUpdate!!.sets.size) {
                                add(
                                    WorkoutSet(
                                        reps = lastOrNull()?.reps ?: 0,
                                        weight = lastOrNull()?.weight
                                    )
                                )
                            }
                        }
                        workoutToUpdate?.let { workout ->
                            onUpdateReps(workout, filledSets)
                        }
                        showRepsDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showRepsDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}