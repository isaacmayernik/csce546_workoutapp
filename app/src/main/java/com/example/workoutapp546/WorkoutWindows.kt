package com.example.workoutapp546

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workoutapp546.screens.Routine
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
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
                .width(300.dp)
                .height(500.dp)
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
                            modifier = Modifier.height(380.dp)
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
                            modifier = Modifier.height(380.dp)
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
                            modifier = Modifier.height(380.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Confirm
                Button(
                    onClick = {
                        val formattedDate = formatDate(selectedYear, selectedMonth, selectedDay)
                        onDateSelected(formattedDate)
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
    modifier: Modifier = Modifier
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
        modifier = modifier
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
                                                    context.getSharedPreferences(
                                                        "app_prefs",
                                                        Context.MODE_PRIVATE
                                                    ),
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