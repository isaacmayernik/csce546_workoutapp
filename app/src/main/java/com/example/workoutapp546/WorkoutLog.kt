package com.example.workoutapp546

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogApp(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("WorkoutApp", Context.MODE_PRIVATE) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val workouts = remember { mutableStateListOf<Workout>() }
    var caloriesConsumed by remember { mutableStateOf("") }

    LaunchedEffect(currentDate) {
        workouts.clear()
        workouts.addAll(loadWorkouts(sharedPreferences, currentDate))
        sharedViewModel.loadGoals(sharedPreferences)
    }

    val currentGoal = sharedViewModel.savedGoals.find { it.date == currentDate }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment =  Alignment.Center
                    ){
                        Text("Workout Log - $currentDate")
                    }
                }
            )
        }
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

            // Calories consumed input
            BasicTextField(
                value = caloriesConsumed,
                onValueChange = { caloriesConsumed = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (caloriesConsumed.isEmpty()) {
                            Text("Enter calories consumed today", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
            )

            Button(
                onClick = {
                    val calories = caloriesConsumed.toIntOrNull()
                    if (calories != null) {
                        sharedViewModel.updateCaloriesConsumed(sharedPreferences, currentDate, calories)
                        caloriesConsumed = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
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

            // Workout input
            var workoutName by remember { mutableStateOf("") }
            var sets by remember { mutableStateOf<List<WorkoutSet>>(emptyList()) }
            var currentSetReps by remember { mutableStateOf("") }

            BasicTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (workoutName.isEmpty()) {
                            Text("Enter workout name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
            )

            // Set input
            BasicTextField(
                value = currentSetReps,
                onValueChange = { currentSetReps = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (currentSetReps.isEmpty()) {
                            Text("Enter reps for this set", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
            )

            Button(
                onClick = {
                    val reps = currentSetReps.toIntOrNull()
                    if (reps != null) {
                        sets = sets + WorkoutSet(reps)
                        currentSetReps = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Set")
            }

            Button(
                onClick = {
                    if (workoutName.isNotBlank() && sets.isNotEmpty()) {
                        val workout = Workout(workoutName, sets)
                        workouts.add(workout)
                        saveWorkouts(sharedPreferences, currentDate, workouts)
                        workoutName = ""
                        sets = emptyList()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Workout")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workout list
            LazyColumn {
                items(workouts) { workout ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(text = workout.name, style = MaterialTheme.typography.titleMedium)
                        workout.sets.forEachIndexed { index, set ->
                            Text(text = "Set ${index + 1}: ${set.reps} reps")
                        }
                    }
                }
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
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface (
           shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Year picker
                Text(text = "Year", style = MaterialTheme.typography.titleMedium)
                ScrollablePicker(
                    items = (1900..2100).toList(),
                    selectedItem = selectedYear,
                    onItemSelected = { selectedYear = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Month picker
                Text(text = "Month", style = MaterialTheme.typography.titleMedium)
                ScrollablePicker(
                    items = (1..12).toList(),
                    selectedItem = selectedMonth + 1,
                    onItemSelected = { selectedMonth = it - 1 }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day picker
                Text(text = "Day", style = MaterialTheme.typography.titleMedium)
                val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)
                ScrollablePicker(
                    items = (1..daysInMonth).toList(),
                    selectedItem = selectedDay,
                    onItemSelected = { selectedDay = it }
                )

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
    onItemSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        val index = items.indexOf(selectedItem)
        if (index != -1) {
            listState.scrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.background
                    )
                    .clickable { onItemSelected(item) }
                    .padding(8.dp)
            ) {
                Text(
                    text = item.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground
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
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getPreviousDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate)!!
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    return dateFormat.format(calendar.time)
}

fun getNextDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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