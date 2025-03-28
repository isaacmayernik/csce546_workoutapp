package com.example.workoutapp546

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Workout(
    val name: String,
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val reps: Int
)

data class Routine(
    val name: String,
    val workouts: List<RoutineWorkout>
)

data class RoutineWorkout(
    val name: String,
    val sets: Int
)

// alternate names for workouts in Select Workout dialog search box
val synonymsMap = mapOf(
    "Bicep curls" to listOf("Barbell curls", "Dumbbell curls"),
    "Cable fly" to "Cable chest fly",
    "Cable tricep pushdown" to "Tricep pushdown",
    "Chest fly" to "Pectoral fly",
    "Chest press" to "Bench press",
    "Chest dips" to "Dips",
    "Chin-ups" to "Pull-ups",
    "Concentration dumbbell curls" to "Preacher curls",
    "Forearm curls" to "Wrist curls",
    "Glute bridge" to "Wide glute bridge",
    "Hammer bicep curls" to "Hammer curl",
    "Incline bench press" to "Incline chest press",
    "Reverse sit ups" to "Reverse crunch",
    "Seated cable row" to "Cable row",
    "Seated dips" to "Tricep dip",
    "Skipping rope" to "Jump rope",
    "Standing calf raises" to "Calf raises",
    "Tricep pushdown" to "Cable tricep pushdown",
    "Walking lunges" to "Lunge"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogApp(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("WorkoutApp", Context.MODE_PRIVATE) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf("") }
    var selectedSets by remember { mutableIntStateOf(0) }
    val workoutNames by remember { mutableStateOf(workoutMuscleMap.keys.toList()) }
    val workouts = remember { mutableStateListOf<Workout>() }
    val muscleStates = remember { mutableStateMapOf<String, Color>() }
    val muscleStatesHistory = remember { mutableStateMapOf<String, MutableList<Pair<Map<String, Color>, List<Workout>>>>() }
    val hasChanges = remember { mutableStateOf(sharedViewModel.hasChangesMap[currentDate] == true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRoutineDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var resetTriggered by remember { mutableStateOf(false) }

    val bodyImage = if (sharedViewModel.isDarkMode) R.drawable.dm_blank_body else R.drawable.blank_body

    LaunchedEffect(currentDate) {
        selectedWorkout = ""
        selectedSets = 0
        workouts.clear()
        workouts.addAll(loadWorkouts(sharedPreferences, currentDate))
        sharedViewModel.loadGoals(sharedPreferences)
        muscleStates.clear()
        muscleStates.putAll(loadMuscleState(sharedPreferences, currentDate))
        sharedViewModel.loadChangesState(sharedPreferences)
        hasChanges.value = sharedViewModel.hasChangesMap[currentDate] == true
        muscleStatesHistory[currentDate]?.clear()
    }

    LaunchedEffect(resetTriggered) {
        if (resetTriggered) {
            muscleStates.clear()
            muscleStates.putAll(loadMuscleState(sharedPreferences, currentDate))
            resetTriggered = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment =  Alignment.Center
                    ){
                        Text(
                            text = "Workout Log: $currentDate",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Date navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { currentDate = getPreviousDate(currentDate) }) {
                    Text("<")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showDatePicker = true }) {
                    Text("Today")
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { currentDate = getNextDate(currentDate) }) {
                    Text(">")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showWorkoutDialog = true },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(selectedWorkout.ifEmpty { "Select Workout" })
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showRoutineDialog = true },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text("Use Routine")
                }
                Spacer(modifier = Modifier.width(8.dp))
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
                            val historyForDate = muscleStatesHistory.getOrPut(currentDate) { mutableStateListOf() }
                            historyForDate.add(Pair(muscleStates.toMap(), workouts.toList()))

                            val affectedMuscles = workoutMuscleMap[selectedWorkout] ?: listOf()
                            val updatedMuscleStates = muscleStates.toMutableMap()
                            affectedMuscles.forEach { muscle ->
                                val currentSets = updatedMuscleStates[muscle]?.let { color ->
                                    when (color) {
                                        Color(0xFF18CB65) -> 0
                                        Color(0xFFA8E02A) -> 1
                                        Color(0xFFFFFF2D) -> 2
                                        Color(0xFFFFD21F) -> 3
                                        Color(0xFFFFB30A) -> 4
                                        Color(0xFFCE3135) -> 5
                                        Color(0xFFCE3135) -> 6
                                        else -> 0
                                    }
                                } ?: 0
                                val totalSets = currentSets + selectedSets

                                updatedMuscleStates[muscle] = getMuscleColor(totalSets)
                            }
                            muscleStates.clear()
                            muscleStates.putAll(updatedMuscleStates)
                            saveMuscleState(sharedPreferences, currentDate, updatedMuscleStates)
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
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = bodyImage,
                    contentDescription = "Full Body Diagram",
                )

                MuscleGroupsView(muscleStates)
            }
        }

        val onUndoLastSave = {
            val historyForDate = muscleStatesHistory[currentDate]
            if (historyForDate != null && historyForDate.isNotEmpty()) {
                val (previousMuscleState, previousWorkouts) = historyForDate.removeAt(historyForDate.size - 1)
                muscleStates.clear()
                muscleStates.putAll(previousMuscleState)
                saveMuscleState(sharedPreferences, currentDate, previousMuscleState)

                workouts.clear()
                workouts.addAll(loadWorkouts(sharedPreferences, currentDate))
                saveWorkouts(sharedPreferences, currentDate, previousWorkouts)

                hasChanges.value = historyForDate.isNotEmpty()
                sharedViewModel.setHasChanges(currentDate, hasChanges.value, sharedPreferences)

                showWorkoutDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Done!")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("No recent changes to undo.")
                }
            }
            Unit
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { year, month, day ->
                    val selectedDate = formatDate(year, month, day)
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
                hasHistory = muscleStatesHistory[currentDate]?.isNotEmpty() == true
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
                            val historyForDate = muscleStatesHistory.getOrPut(currentDate) { mutableStateListOf() }
                            historyForDate.add(Pair(muscleStates.toMap(), workouts.toList()))

                            routine.workouts.forEach { workout ->
                                val affectedMuscles = workoutMuscleMap[workout.name] ?: listOf()
                                affectedMuscles.forEach { muscle ->
                                    val currentSets = muscleStates[muscle]?.let { color ->
                                        when (color) {
                                            Color(0xFF18CB65) -> 0
                                            Color(0xFFA8E02A) -> 1
                                            Color(0xFFFFFF2D) -> 2
                                            Color(0xFFFFD21F) -> 3
                                            Color(0xFFFFB30A) -> 4
                                            Color(0xFFCE3135) -> 5
                                            Color(0xFFCE3135) -> 6
                                            else -> 0
                                        }
                                    } ?: 0
                                    val totalSets = currentSets + workout.sets
                                    muscleStates[muscle] = getMuscleColor(totalSets)
                                }
                                workouts.add(Workout(workout.name, List(workout.sets) { WorkoutSet(0) }))
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
                            muscleStatesHistory[currentDate]?.clear()
                            muscleStates.clear()
                            saveMuscleState(sharedPreferences, currentDate, muscleStates)
                            workouts.clear()
                            saveWorkouts(sharedPreferences, currentDate, workouts)

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

@Composable
fun MuscleGroupsView(muscleStates: Map<String, Color>) {
    // Map muscle names to their respective drawable resources
    val muscleDrawableMap = mapOf(
        "ab" to R.drawable.abs,
        "back-lower" to R.drawable.back_lower,
        "bicep" to R.drawable.bicep,
        "calf" to R.drawable.calves,
        "chest-right" to R.drawable.chest_right,
        "chest-left" to R.drawable.chest_left,
        "deltoids-rear" to R.drawable.deltoids_rear,
        "forearm" to R.drawable.forearms,
        "glute" to R.drawable.glutes,
        "hamstring" to R.drawable.hamstring,
        "hip" to R.drawable.hip,
        "oblique" to R.drawable.oblique,
        "pronator teres" to R.drawable.pronatur_teres,
        "rectus-abdominus" to R.drawable.rectus_abdominus,
        "rectus-femoris" to R.drawable.rectus_femoris,
        "shoulder-deltoid" to R. drawable.shoulder_deltoids,
        "sternocleidomastoid" to R.drawable.sternocleidomastoid,
        "thigh" to R.drawable.thigh,
        "trapezius" to R.drawable.trapezius,
        "tricep" to R.drawable.tricep,
        "vastus-lateralis" to R.drawable.vastus_lateralis,
        "vastus-medialis" to R.drawable.vastus_medialis,
    )

    // Iterate through muscleStates and display each muscle group
    muscleStates.forEach { (muscle, color) ->
        val drawableRes = muscleDrawableMap[muscle]
        if (drawableRes != null) {
            AsyncImage(
                model = drawableRes,
                contentDescription = muscle,
                modifier = Modifier
                    .fillMaxSize(),
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
}

// Dropdown of routines
@Composable
fun RoutineDialog(
    routines: List<Routine>,
    onRoutineSelected: (Routine) -> Unit,
    onDismissRequest: () -> Unit,
    sharedViewModel: SharedViewModel,
    context: Context,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                if (routines.isEmpty()) {
                    Text("No routines found", modifier = Modifier.padding(8.dp))
                } else {
                    LazyColumn {
                        items(routines) { routine ->
                            var showDelete by remember { mutableStateOf(false) }

                            SwipeToReveal(
                                onReveal = { showDelete = true },
                                onDismiss = { showDelete = false },
                                content = {
                                    Text(
                                        text = routine.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onRoutineSelected(routine) }
                                            .padding(8.dp)
                                    )
                                },
                                actionContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Red)
                                            .clickable {
                                                sharedViewModel.removeRoutine(
                                                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE),
                                                    routine
                                                )
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Routine deleted")
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Delete", color = Color.White)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dropdown for Select Workout (list of workouts from MuscleColorManager)
@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun WorkoutDialog(
    selectedWorkout: String,
    onWorkoutSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    workoutNames: List<String>,
    onResetWorkouts: () -> Unit,
    onUndoLastSave: () -> Unit,
    hasChanges: Boolean,
    hasHistory: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSortDialog by remember { mutableStateOf(false) }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

    val filteredWorkouts = if (searchQuery.isEmpty()) {
        workoutNames
    } else {
        workoutNames.filter { workout ->
            workout.contains(searchQuery, ignoreCase = true) ||
                    synonymsMap.any { (synonym, mappedWorkout) ->
                        synonym.contains(searchQuery, ignoreCase = true) && mappedWorkout == workout
                    }
        }
    }

    val sortedWorkouts = if (selectedMuscleGroup == "All") {
        filteredWorkouts
    } else {
        filteredWorkouts.filter { workout ->
            workoutMuscleMap[workout]?.contains(selectedMuscleGroup) == true
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    // Search bar
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(.75f)
                            .padding(end = 8.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp)
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search workouts...",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Sort button
                    Button(
                        onClick = { showSortDialog = true },
                        modifier = Modifier.width(94.dp)
                    ) {
                        Text("Sort By")
                    }
                }

                // if sorted by muscle group, display a Clear Sort button
                if (selectedMuscleGroup != "All") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Sorted by:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                text = selectedMuscleGroup
                                    .replace("-", " ")
                                    .replaceFirstChar { it.uppercaseChar() },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Button(
                            onClick = { selectedMuscleGroup = "All" },
                            modifier = Modifier.width(90.dp)
                        ) {
                            Text("Clear")
                        }
                    }
                }

                // Filtered workout list
                if (sortedWorkouts.isEmpty()) {
                    Text("No workouts found", modifier = Modifier.padding(8.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(sortedWorkouts) { workout ->
                            var isHovered by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isHovered) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                when (event.type) {
                                                    PointerEventType.Enter -> isHovered = true
                                                    PointerEventType.Exit -> isHovered = false
                                                    else -> Unit
                                                }
                                            }
                                        }
                                    }
                                    .clickable {
                                        onWorkoutSelected(workout)
                                        onDismissRequest()
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = workout,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onUndoLastSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = hasChanges
                ) {
                    Text("Undo Last Save")
                }

                Button(
                    onClick = onResetWorkouts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = hasHistory
                ) {
                    Text("Reset Workouts")
                }
            }
        }
    }

    if (showSortDialog) {
        SortByDialog(
            onDismissRequest = { showSortDialog = false },
            onMuscleGroupSelected = { muscleGroup ->
                selectedMuscleGroup = muscleGroup
            }
        )
    }
}

// Dialog for sorting through muscle groups
@Composable
fun SortByDialog(
    onDismissRequest: () -> Unit,
    onMuscleGroupSelected: (String) -> Unit
) {
    val muscleGroups = workoutMuscleMap.values.flatten().toSet().toList().sortedBy { originalName ->
        when {
            originalName == "back-lower" -> "lower back"
            originalName == "deltoids-rear" -> "rear deltoids"
            originalName.contains("chest-") -> "chest"
            else -> originalName
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp),
        ){
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text= "Sort by muscle group",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(muscleGroups.distinctBy {
                        when {
                            it.contains("chest-") -> "chest"
                            else -> it
                        }
                    }) { muscle ->
                        val formattedMuscle = when {
                            muscle == "deltoids-rear" -> "Rear Deltoids"
                            muscle == "back-lower" -> "Lower Back"
                            muscle.contains("chest-") -> "Chest"
                            else -> muscle
                                .replace("-", " ")
                                .split(" ")
                                .joinToString(" ") { word ->
                                    word.replaceFirstChar { it.uppercaseChar() }
                                }
                        }

                        Text(
                            text = formattedMuscle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val originalName = when (formattedMuscle) {
                                        "Chest" -> if (muscle.contains("left")) "chest-left" else "chest-right"
                                        "Rear Deltoids" -> "deltoids-rear"
                                        "Lower Back" -> "back-lower"
                                        else -> muscle
                                    }
                                    onMuscleGroupSelected(originalName)
                                    onDismissRequest()
                                }
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface (
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .padding(16.dp)
                .width(450.dp)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(75.dp)
                    ) {
                        Text(text = "Month", style = MaterialTheme.typography.titleMedium)
                        ScrollablePicker(
                            items = (1..12).toList(),
                            selectedItem = selectedMonth + 1,
                            onItemSelected = { selectedMonth = it - 1 },
                        )
                    }

                    // Day picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(75.dp)
                    ) {
                        Text(text = "Day", style = MaterialTheme.typography.titleMedium)
                        val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)
                        ScrollablePicker(
                            items = (1..daysInMonth).toList(),
                            selectedItem = selectedDay,
                            onItemSelected = { selectedDay = it },
                        )
                    }

                    // Year picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(75.dp)
                    ) {
                        Text(text = "Year", style = MaterialTheme.typography.titleMedium)
                        ScrollablePicker(
                            items = (2000..2100).toList(),
                            selectedItem = selectedYear,
                            onItemSelected = { selectedYear = it },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Confirm
                Button(
                    onClick = {
                        onDateSelected(selectedYear, selectedMonth, selectedDay)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Date")
                }
            }
        }
    }
}

@Composable
fun ScrollablePicker(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        val index = items.indexOf(selectedItem)
        if (index != -1) {
            listState.scrollToItem(index)
        }
    }

    LazyColumn(
        state = listState
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.background
                    )
                    .clickable { onItemSelected(item) }
                    .padding(8.dp)
            ) {
                Text(
                    text = item.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SwipeToReveal(
    onReveal: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
    actionContent: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxOffset = 100.dp
    val maxOffsetPx = with(LocalDensity.current) { maxOffset.toPx() }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Delete button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(maxOffset)
        ) {
            actionContent()
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -maxOffsetPx / 2) {
                                offsetX = -maxOffsetPx
                                onReveal()
                            } else {
                                offsetX = 0f
                                onDismiss()
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxOffsetPx, 0f)
                        }
                    )
                }
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

fun getDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun formatDate(year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getPreviousDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate)!!
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    return dateFormat.format(calendar.time)
}

fun getNextDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate)!!
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    return dateFormat.format(calendar.time)
}

fun saveWorkouts(sharedPreferences: SharedPreferences, date: String, workouts: List<Workout>) {
    val gson = Gson()
    val json = gson.toJson(workouts)
    sharedPreferences.edit { putString(date, json) }
}

fun loadWorkouts(sharedPreferences: SharedPreferences, date: String): List<Workout> {
    val gson = Gson()
    val json = sharedPreferences.getString(date, null)
    return if (json != null) {
        val type = object : TypeToken<List<Workout>>() {}.type
        gson.fromJson(json, type)
    } else {
        emptyList()
    }
}