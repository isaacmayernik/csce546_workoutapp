package com.example.workoutapp546.screens

import android.content.SharedPreferences
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.workoutapp546.SharedViewModel
import com.example.workoutapp546.loadMuscleState
import com.example.workoutapp546.loadWorkouts
import com.example.workoutapp546.saveMuscleState
import com.example.workoutapp546.saveWorkouts
import com.example.workoutapp546.updateMuscleStatesAfterDeletion

@Composable
fun WorkoutHistory(
    sharedViewModel: SharedViewModel,
    navController: NavHostController,
    date: String,
    sharedPreferences: SharedPreferences
) {
    var workouts by remember { mutableStateOf(loadWorkouts(sharedPreferences, date).first) }
    val groupedWorkouts = remember(workouts) {
        workouts.groupBy { it.name }.map { (name, workouts) ->
            val allSets = workouts.flatMap { it.sets }
            val pr = sharedViewModel.personalRecords[name]
            val isNewPR = pr?.let { prRecord ->
                allSets.any { set ->
                    set.weight?.let { weight ->
                        (set.reps * weight) >= (prRecord.reps * prRecord.weight)
                    } ?: false
                }
            } ?: false

            GroupedWorkout(
                name = name,
                sets = allSets,
                isNewPR = isNewPR,
                prRecord = pr?.let {
                    "${it.reps} reps @ ${if (it.weight.rem(1) == 0f) it.weight.toInt() else it.weight}lbs"
                }
            )
        }
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showRepsDialog by remember { mutableStateOf(false) }
    var workoutToDelete by remember { mutableStateOf<String?>(null) }
    var workoutToUpdate by remember { mutableStateOf<String?>(null) }

    fun handleDeleteWorkout(name: String) {
        val workoutsToDelete = workouts.filter { it.name == name }
        val updatedWorkouts = workouts.filterNot { it.name == name }.toMutableList()
        saveWorkouts(sharedPreferences, date, updatedWorkouts)
        workouts = updatedWorkouts

        val muscleStates = loadMuscleState(sharedPreferences, date)
        workoutsToDelete.forEach { workout ->
            updateMuscleStatesAfterDeletion(workout, muscleStates)
        }
        saveMuscleState(sharedPreferences, date, muscleStates)
    }

    fun handleUpdateReps(name: String, sets: List<WorkoutSet>) {
        val updatedWorkouts = workouts.filterNot { it.name == name }.toMutableList()

        sets.forEach { set ->
            updatedWorkouts.add(Workout(name = name, sets = listOf(set)))
        }

        saveWorkouts(sharedPreferences, date, updatedWorkouts)
        workouts = updatedWorkouts

        updatedWorkouts.filter { it.name == name }.forEach { workout ->
            if (sharedViewModel.checkNewPR(workout)) {
                sharedViewModel.savePR(sharedPreferences)
            }
        }
    }

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
                    items(groupedWorkouts) { groupedWorkout ->
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
                                    Text(groupedWorkout.name, style = MaterialTheme.typography.titleMedium)
                                    if (groupedWorkout.isNewPR) {
                                        sharedViewModel.personalRecords[groupedWorkout.name]?.let { pr ->
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("New PR!", color = Color.Green,
                                                    style = MaterialTheme.typography.labelLarge)
                                                groupedWorkout.prRecord?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                groupedWorkout.sets.forEachIndexed { index, set ->
                                    if (index == 0 || set.reps > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Set ${index + 1}: ${set.reps} reps")
                                            Spacer(modifier = Modifier.width(4.dp))

                                            set.weight?.let { weight ->
                                                val weightText = if (weight.rem(1) == 0f) {
                                                    weight.toInt().toString()
                                                } else {
                                                    weight.toString()
                                                }
                                                Text(" @ ${weightText}lbs", style = MaterialTheme.typography.bodySmall)
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
                                            workoutToUpdate = groupedWorkout.name
                                            showRepsDialog = true
                                        }
                                    ) { Text("Edit Reps") }

                                    Button(
                                        onClick = {
                                            workoutToDelete = groupedWorkout.name
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
            text = { Text("Are you sure you want to delete all sets of $workoutToDelete?") },
            confirmButton = {
                Button(
                    onClick = {
                        workoutToDelete?.let { handleDeleteWorkout(it) }
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
        val exerciseSets = remember {
            mutableStateListOf<WorkoutSet>().apply {
                addAll(workouts.filter { it.name == workoutToUpdate }.flatMap { it.sets })
            }
        }

        AlertDialog(
            onDismissRequest = { showRepsDialog = false },
            title = { Text("Edit sets for $workoutToUpdate", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    exerciseSets.forEachIndexed { index, set ->
                        var repsText by remember { mutableStateOf(set.reps.toString()) }
                        var weightText by remember {
                            mutableStateOf(
                                if(set.weight?.rem(1) == 0.0f)
                                    set.weight.toInt().toString()
                                else
                                    set.weight?.toString() ?: ""
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .height(56.dp)
                        ) {
                            Text("Set ${index + 1}: ", modifier = Modifier.width(50.dp))

                            // Reps input
                            OutlinedTextField(
                                value = repsText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty()) {
                                        repsText = ""
                                        exerciseSets[index] = set.copy(reps = 0)
                                    } else if (newValue.all { it.isDigit() }) {
                                        repsText = newValue
                                        exerciseSets[index] = set.copy(reps = newValue.toInt())
                                    }
                                },
                                label = { Text("Reps") },
                                modifier = Modifier.width(86.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Weight input
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty()) {
                                        weightText = ""
                                        exerciseSets[index] = set.copy(weight = null)
                                    } else if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                        weightText = newValue
                                        exerciseSets[index] = set.copy(
                                            weight = newValue.toFloatOrNull()
                                        )
                                    }
                                },
                                label = { Text("Weight") },
                                modifier = Modifier.width(86.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true
                            )

                            IconButton(
                                onClick = { exerciseSets.removeAt(index) },
                                enabled = exerciseSets.size > 1
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove set")
                            }
                        }
                    }

                    Button(
                        onClick = { exerciseSets.add(WorkoutSet(
                            reps = exerciseSets.lastOrNull()?.reps ?: 1,
                            weight = exerciseSets.lastOrNull()?.weight )
                        )
                        },
                        enabled = exerciseSets.size < workouts.filter { it.name == workoutToUpdate }.flatMap { it.sets }.size,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Add Set")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        workoutToUpdate?.let { name ->
                            handleUpdateReps(name, exerciseSets)
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