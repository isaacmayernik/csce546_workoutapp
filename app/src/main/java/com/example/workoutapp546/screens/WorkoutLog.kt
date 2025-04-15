package com.example.workoutapp546.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.workoutapp546.AnimationState
import com.example.workoutapp546.DatePickerDialog
import com.example.workoutapp546.MuscleGroupsView
import com.example.workoutapp546.R
import com.example.workoutapp546.RoutineDialog
import com.example.workoutapp546.SharedViewModel
import com.example.workoutapp546.WorkoutDialog
import com.example.workoutapp546.getColorTransition
import com.example.workoutapp546.getCurrentDate
import com.example.workoutapp546.getMuscleColor
import com.example.workoutapp546.getNextDate
import com.example.workoutapp546.getPreviousDate
import com.example.workoutapp546.loadMuscleState
import com.example.workoutapp546.loadWorkouts
import com.example.workoutapp546.saveMuscleState
import com.example.workoutapp546.saveWorkouts
import com.example.workoutapp546.workoutMuscleMap
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GroupedWorkout(
    val name: String,
    val sets: List<WorkoutSet>,
    val isNewPR: Boolean,
    val prRecord: String?
)

data class Workout(
    val name: String,
    val sets: List<WorkoutSet>,
    val weight: Float? = null
)

data class WorkoutSet(
    val reps: Int,
    val weight: Float? = null
)

data class Routine(
    val name: String,
    val workouts: List<RoutineWorkout>
)

data class RoutineWorkout(
    val name: String,
    val sets: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogApp(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("WorkoutApp", Context.MODE_PRIVATE) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf("") }
    var selectedSets by remember { mutableIntStateOf(0) }
    val workoutNames by remember { mutableStateOf(workoutMuscleMap.keys.toList()) }
    val workouts = remember { mutableStateListOf<Workout>() }
    val muscleStates = remember {
        mutableStateMapOf<String, Color>().apply {
            workoutMuscleMap.values.map { it.first}.flatten().toSet().forEach { muscle ->
                this[muscle] = Color(0xFF18CB65)
            }
        }
    }
    val workoutHistory =
        remember { mutableStateMapOf<String, MutableList<Pair<Map<String, Color>, List<Workout>>>>() }
    val hasChanges = remember { mutableStateOf(sharedViewModel.hasChangesMap[currentDate] == true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRoutineDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var resetTriggered by remember { mutableStateOf(false) }

    val bodyImage =
        if (sharedViewModel.isDarkMode) R.drawable.dm_blank_body else R.drawable.blank_body

    var animationState by remember { mutableStateOf(AnimationState()) }
    var targetSets by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentDate) {
        selectedWorkout = ""
        selectedSets = 0

        val (loadedWorkouts, loadedMuscleSets, hasSavedWorkouts) = loadWorkouts(
            sharedPreferences,
            currentDate
        )
        workouts.clear()
        workouts.addAll(loadedWorkouts)
        val savedMuscleStates = loadMuscleState(sharedPreferences, currentDate)
        muscleStates.clear()
        muscleStates.putAll(savedMuscleStates)

        workoutMuscleMap.values.map { it.first }.flatten().toSet().forEach { muscle ->
            if (!muscleStates.containsKey(muscle)) {
                muscleStates[muscle] = Color(0xFF18CB65)
            }
        }

        loadedMuscleSets.forEach { (muscle, sets) ->
            muscleStates[muscle] = getMuscleColor(sets)
        }

        workoutHistory[currentDate] = if (hasSavedWorkouts) {
            mutableStateListOf(Pair(muscleStates.toMap(), loadedWorkouts.toList()))
        } else {
            mutableStateListOf()
        }

        sharedViewModel.loadGoals(sharedPreferences)
        sharedViewModel.loadChangesState(sharedPreferences)
        sharedViewModel.setHasChanges(currentDate, hasSavedWorkouts, sharedPreferences)
        hasChanges.value = hasSavedWorkouts
    }

    LaunchedEffect(resetTriggered) {
        if (resetTriggered) {
            muscleStates.clear()
            muscleStates.putAll(loadMuscleState(sharedPreferences, currentDate))
            resetTriggered = false
        }
    }

    LaunchedEffect(animationState.isAnimating) {
        if (animationState.isAnimating) {
            val affectedMuscles = workoutMuscleMap[selectedWorkout]?.first ?: listOf()

            affectedMuscles.forEach { muscle ->
                val currentColor = muscleStates[muscle] ?: Color(0xFF18CB65)
                val currentSets = when (currentColor) {
                    Color(0xFF18CB65) -> 0
                    Color(0xFFA8E02A) -> 1
                    Color(0xFFFFFF2D) -> 2
                    Color(0xFFFFD21F) -> 3
                    Color(0xFFFFB30A) -> 4
                    Color(0xFFCE3135) -> 5
                    else -> 0
                }

                val targetSets = currentSets + selectedSets
                val steps = getColorTransition(currentSets, targetSets)

                steps.forEach { color ->
                    animationState = animationState.copy(
                        currentMuscle = muscle,
                        currentColor = color
                    )
                    muscleStates[muscle] = color
                    delay(300) // Reduced delay for smoother animation
                }
            }

            // Save final state
            saveMuscleState(sharedPreferences, currentDate, muscleStates)
            animationState = AnimationState()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))

                // Date navigation
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { currentDate = getPreviousDate(currentDate) }) {
                        Text("<")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(currentDate)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { currentDate = getNextDate(currentDate) }) {
                        Text(">")
                    }
                }

                IconButton(
                    onClick = {
                        if (workouts.isNotEmpty() || workoutHistory[currentDate]?.isNotEmpty() == true) {
                            navController.navigate("history/${currentDate}")
                        }
                    },
                    enabled = workouts.isNotEmpty() || workoutHistory[currentDate]?.isNotEmpty() == true,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Workout History",
                        tint = if (workouts.isNotEmpty() || workoutHistory[currentDate]?.isNotEmpty() == true)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showWorkoutDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedWorkout.ifEmpty { "Select Workout" })
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { showRoutineDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use Routine")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (selectedSets > 0) selectedSets--
                    }
                ) {
                    Text("-")
                }
                Text("$selectedSets sets", Modifier.padding(16.dp))
                Button(
                    onClick = { selectedSets++ }
                ) {
                    Text("+")
                }

                Button(
                    onClick = {
                        if (selectedWorkout.isEmpty()) {
                            // launch a pop-up if user has not selected a workout and tries saving workout
                            scope.launch {
                                snackbarHostState.showSnackbar("Please select a workout from the dropdown.")
                            }
                        } else if (selectedSets > 0) {
                            val historyForDate =
                                workoutHistory.getOrPut(currentDate) { mutableStateListOf() }
                            historyForDate.add(Pair(muscleStates.toMap(), workouts.toList()))

                            val newWorkout = Workout(selectedWorkout, List(selectedSets) { WorkoutSet(0) })
                            workouts.add(newWorkout)

                            targetSets = selectedSets
                            targetSets = selectedSets
                            animationState = animationState.copy(isAnimating = true)

                            saveWorkouts(sharedPreferences, currentDate, workouts)
                            saveMuscleState(sharedPreferences, currentDate, muscleStates)
                            sharedViewModel.setHasChanges(currentDate, true, sharedPreferences)
                            hasChanges.value = true
                            sharedViewModel.saveChangesState(sharedPreferences)
                        }
                    }
                ) {
                    Text("Save Workout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        AsyncImage(
                            model = bodyImage,
                            contentDescription = "Full Body Diagram",
                            modifier = Modifier.fillMaxSize()
                        )
                        MuscleGroupsView(muscleStates, animationState)
                    }
                }
            }
        }

        val onUndoLastSave = {
            val historyForDate = workoutHistory[currentDate]
            if (historyForDate != null && historyForDate.size > 1) {
                val (previousMuscleState, previousWorkouts) = historyForDate.removeAt(historyForDate.size - 1)

                muscleStates.clear()
                muscleStates.putAll(previousMuscleState)
                saveMuscleState(sharedPreferences, currentDate, previousMuscleState)

                workouts.clear()
                workouts.addAll(previousWorkouts)

                val muscleSets = mutableMapOf<String, Int>()
                previousWorkouts.forEach { workout ->
                    val muscles = workoutMuscleMap[workout.name]?.first ?: listOf()
                    muscles.forEach { muscle ->
                        muscleSets[muscle] = (muscleSets[muscle] ?: 0) + workout.sets.size
                    }
                }

                val gson = Gson()
                sharedPreferences.edit {
                    putString(currentDate, gson.toJson(previousWorkouts))
                    putString("${currentDate}_muscle_sets", gson.toJson(muscleSets))
                }

                hasChanges.value = historyForDate.size > 1
                sharedViewModel.setHasChanges(currentDate, hasChanges.value, sharedPreferences)

                scope.launch {
                    snackbarHostState.showSnackbar("Changes undone")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Nothing to undo")
                }
            }
            Unit
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { selectedDate ->
                    currentDate = selectedDate
                    showDatePicker = false
                }
            )
        }

        if (showWorkoutDialog) {
            WorkoutDialog(
                selectedWorkout = selectedWorkout,
                onWorkoutSelected = { workout ->
                    selectedWorkout = workout
                },
                onDismissRequest = { showWorkoutDialog = false },
                workoutNames = workoutNames,
                onResetWorkouts = { showResetConfirmation = true },
                onUndoLastSave = onUndoLastSave,
                hasChanges = hasChanges.value,
                hasHistory = workouts.isNotEmpty() || workoutHistory[currentDate]?.isNotEmpty() == true
            )
        }

        if (showRoutineDialog) {
            RoutineDialog(
                routines = sharedViewModel.savedRoutines,
                onRoutineSelected = { routine ->
                    // Show confirmation dialog before adding
                    showRoutineDialog = false
                    scope.launch {
                        val confirm = withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Add ${routine.name} to today's workout?",
                                actionLabel = "Add"
                            ) == SnackbarResult.ActionPerformed
                        }
                        if (confirm) {
                            val historyForDate =
                                workoutHistory.getOrPut(currentDate) { mutableStateListOf() }
                            historyForDate.add(Pair(muscleStates.toMap(), workouts.toList()))

                            routine.workouts.forEach { workout ->
                                targetSets = workout.sets
                                animationState = animationState.copy(isAnimating = true)

                                while (animationState.isAnimating) {
                                    delay(100)
                                }

                                workouts.add(
                                    Workout(
                                        workout.name,
                                        List(workout.sets) { WorkoutSet(0) })
                                )
                            }

                            saveMuscleState(sharedPreferences, currentDate, muscleStates)
                            saveWorkouts(sharedPreferences, currentDate, workouts)
                            sharedViewModel.setHasChanges(currentDate, true, sharedPreferences)
                            hasChanges.value = true
                            snackbarHostState.showSnackbar("Added ${routine.name} to workout")
                        }
                    }
                },
                onDismissRequest = { showRoutineDialog = false },
                sharedViewModel = sharedViewModel,
                context = context
            )
        }

        if (showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { showResetConfirmation = false },
                title = { Text("Reset Workouts") },
                text = { Text("Are you sure you want to reset all workouts for today, $currentDate?") },
                confirmButton = {
                    Button(
                        onClick = {
                            workoutHistory[currentDate]?.clear()
                            workouts.clear()
                            muscleStates.clear()
                            workoutMuscleMap.values.map { it.first }.flatten().toSet().forEach { muscle ->
                                muscleStates[muscle] = Color(0xFF18CB65)
                            }
                            saveMuscleState(sharedPreferences, currentDate, muscleStates)

                            sharedPreferences.edit {
                                remove(currentDate)
                                remove("${currentDate}_muscle_sets")
                                remove("muscle_state_$currentDate")
                            }

                            hasChanges.value = false
                            sharedViewModel.setHasChanges(currentDate, false, sharedPreferences)
                            showWorkoutDialog = false
                            resetTriggered = true
                            showResetConfirmation = false

                            scope.launch {
                                snackbarHostState.showSnackbar("Done!")
                            }
                        }
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showResetConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}