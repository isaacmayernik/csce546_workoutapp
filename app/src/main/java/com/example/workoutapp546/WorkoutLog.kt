package com.example.workoutapp546

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var caloriesConsumed by remember { mutableStateOf("") }
    val muscleStates = remember { mutableStateMapOf<String, Color>() }
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
    }

    LaunchedEffect(resetTriggered) {
        if (resetTriggered) {
            muscleStates.clear()
            muscleStates.putAll(loadMuscleState(sharedPreferences, currentDate))
            resetTriggered = false
        }
    }

    val currentGoal = sharedViewModel.savedGoals.find { it.date == currentDate } ?: sharedViewModel.savedGoals.maxByOrNull { it.date }

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calories consumed input
                BasicTextField(
                    value = caloriesConsumed,
                    onValueChange = { caloriesConsumed = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (caloriesConsumed.isEmpty()) {
                                Text(
                                    text = "Enter calories consumed today",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save calories button
                Button(
                    onClick = {
                        val calories = caloriesConsumed.toIntOrNull()
                        if (calories != null) {
                            sharedViewModel.updateCaloriesConsumed(sharedPreferences, currentDate, calories)
                            caloriesConsumed = ""
                        }
                    },
                ) {
                    Text("Save Calories")
                }

                // Display calories left for today
                if (currentGoal != null) {
                    Text(
                        text = "Calories Left: ${currentGoal.calorieGoal - currentGoal.caloriesConsumed} cal",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
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
                horizontalArrangement = Arrangement.SpaceAround
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
                            val affectedMuscles = workoutMuscleMap[selectedWorkout] ?: listOf()
                            val updatedMuscleStates = muscleStates.toMutableMap()
                            affectedMuscles.forEach { muscle ->
                                val currentSets = updatedMuscleStates[muscle]?.let { color ->
                                    when (color) {
                                        Color.Red -> 3
                                        Color(0xFFFFA500) -> 2
                                        Color.Yellow -> 1
                                        else -> 0
                                    }
                                }?: 0
                                val totalSets = currentSets + selectedSets

                                updatedMuscleStates[muscle] = getMuscleColor(totalSets)
                            }
                            muscleStates.clear()
                            muscleStates.putAll(updatedMuscleStates)
                            saveMuscleState(sharedPreferences, currentDate, updatedMuscleStates)
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
                onResetWorkouts = { showResetConfirmation = true }
            )
        }

        if (showRoutineDialog) {
            RoutineDialog(
                routines = sharedViewModel.savedRoutines,
                onRoutineSelected = { routine ->
                    selectedWorkout = routine.workouts.joinToString { it.name }
                    showRoutineDialog = false
                },
                onDismissRequest = { showRoutineDialog = false }
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
                            muscleStates.clear()
                            saveMuscleState(sharedPreferences, currentDate, muscleStates)
                            workouts.clear()
                            saveWorkouts(sharedPreferences, currentDate, workouts)

                            resetTriggered = true
                            showResetConfirmation = false
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
    onDismissRequest: () -> Unit
) {
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
                            Text(
                                text = routine.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRoutineSelected(routine) }
                                    .padding(8.dp)
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
    onResetWorkouts: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredWorkouts = if (searchQuery.isEmpty()) {
        workoutNames
    } else {
        workoutNames.filter { it.contains(searchQuery, ignoreCase = true) }
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
                // Search bar
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
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

                // Filtered workout list
                if (filteredWorkouts.isEmpty()) {
                    Text("No workouts found", modifier = Modifier.padding(8.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(filteredWorkouts) { workout ->
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
                    onClick = onResetWorkouts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Reset Workouts")
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
    sharedPreferences.edit().putString(date, json).apply()
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