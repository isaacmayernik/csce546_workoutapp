package com.example.workoutapp546.screens

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.workoutapp546.Screen
import com.example.workoutapp546.SharedViewModel
import com.example.workoutapp546.getCurrentDate
import com.example.workoutapp546.ui.theme.Dimensions
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Goal(
    val weightGoal: Double,
    val currentWeight: Double,
    val activityLevel: String,
    val calorieGoal: Int,
    val caloriesConsumed: Int,
    val description: String,
    val date: String,
    val lastUpdated: String,
)

@Composable
fun Goals(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("GoalsApp", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var weightGoal by remember { mutableStateOf("") }
    var currentWeight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("") }
    var calorieGoal by remember { mutableStateOf("") }
    var caloriesConsumed by remember { mutableStateOf("") }
    var showCaloriesLeft by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }

    // text styles
    val bodyTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = Dimensions.scaledSp(Dimensions.bodyTextSize)
    )
    val buttonTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = Dimensions.scaledSp(Dimensions.buttonTextSize)
    )
    val placeholderTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = Dimensions.scaledSp(Dimensions.bodyTextSize),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

    LaunchedEffect(Unit) {
        sharedViewModel.loadGoals(sharedPreferences)
    }

    val savedGoals = sharedViewModel.savedGoals
    val currentGoal = savedGoals.find { it.date == getCurrentDate() }

    fun formatDouble(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    LaunchedEffect(currentGoal) {
        if (currentGoal != null) {
            currentWeight = formatDouble(currentGoal.currentWeight)
            weightGoal = formatDouble(currentGoal.weightGoal)
            activityLevel = currentGoal.activityLevel
            calorieGoal = currentGoal.calorieGoal.toString()
            caloriesConsumed = currentGoal.caloriesConsumed.toString()
            description = currentGoal.description
            hasChanges = false

            if (currentGoal.caloriesConsumed > 0) {
                showCaloriesLeft = true
            }
        }
    }

    fun saveGoal() {
        val weight = weightGoal.toDoubleOrNull()
        val current = currentWeight.toDoubleOrNull()
        val calories = calorieGoal.toIntOrNull()
        val consumed = caloriesConsumed.toIntOrNull()
        if (weight != null && current != null && calories != null && activityLevel.isNotEmpty()) {
            val currentDate = getCurrentDate()
            val goal = Goal(
                weightGoal = weight,
                currentWeight = current,
                activityLevel = activityLevel,
                calorieGoal = calories,
                caloriesConsumed = consumed ?: 0,
                description = description,
                date = currentDate,
                lastUpdated = getCurrentDateTime()
            )

            val updatedGoals = savedGoals.filter { it.date != currentDate } + goal
            sharedViewModel.saveGoals(sharedPreferences, updatedGoals)
            hasChanges = false
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Please fill in all the required fields to view a graph.")
            }
        }
    }

    // When we change to any other screen, save goals inputted
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.route != Screen.Goals.route && hasChanges) {
                saveGoal()
            }
        }
    }

    // Automatically saves goals/info
    LaunchedEffect(currentWeight, weightGoal, activityLevel, calorieGoal, description) {
        delay(5000)
        saveGoal()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (currentGoal != null) {
                Text(
                    text = "Last Updated: ${currentGoal.lastUpdated}",
                    style = bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Weight",
                    modifier = Modifier.weight(1f),
                    style = bodyTextStyle
                )
                // Current weight input
                BasicTextField(
                    value = currentWeight,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            currentWeight = newValue
                            hasChanges = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .width(250.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (currentWeight.isEmpty()) {
                                Text(
                                    "Enter current weight",
                                    style = placeholderTextStyle
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = bodyTextStyle
                )
            }

            // Weight goal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal Weight",
                    modifier = Modifier.weight(1f),
                    style = bodyTextStyle
                )
                // Weight goal input
                BasicTextField(
                    value = weightGoal,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            weightGoal = newValue
                            hasChanges = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .width(250.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (weightGoal.isEmpty()) {
                                Text(
                                    "Enter weight goal",
                                    style = placeholderTextStyle
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = bodyTextStyle
                )
            }

            // Activity level
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Level",
                    modifier = Modifier.weight(0.4f),
                    style = bodyTextStyle
                )
                // Activity level input
                val activityLevels =
                    listOf("Very Active", "Active", "Lightly Active", "Not Very Active")
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.scaledDp(Dimensions.buttonHeight))
                            .padding(Dimensions.scaledDp(Dimensions.buttonPadding))
                    ) {
                        Text(
                            activityLevel.ifEmpty { "Select activity level" },
                            style = buttonTextStyle,
                            modifier = Modifier.wrapContentSize(Alignment.Center)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        activityLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level, style = bodyTextStyle) },
                                onClick = {
                                    activityLevel = level
                                    expanded = false
                                    hasChanges = true
                                }
                            )
                        }
                    }
                }
            }

            // Calorie goal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calorie Goal",
                    modifier = Modifier.weight(1f),
                    style = bodyTextStyle
                )
                // Calorie goal input
                BasicTextField(
                    value = calorieGoal,
                    onValueChange = {
                        calorieGoal = it
                        hasChanges = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .width(250.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (calorieGoal.isEmpty()) {
                                Text(
                                    "Enter a daily calorie goal",
                                    style = placeholderTextStyle
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = bodyTextStyle
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calories Consumed",
                    modifier = Modifier.weight(1f),
                    style = bodyTextStyle
                )
                BasicTextField(
                    value = caloriesConsumed,
                    onValueChange = { caloriesConsumed = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .width(250.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (caloriesConsumed.isEmpty()) {
                                Text(
                                    "Enter calories consumed",
                                    style = placeholderTextStyle
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = bodyTextStyle
                )
                Button(
                    onClick = {
                        val consumed = caloriesConsumed.toIntOrNull()
                        val goal = calorieGoal.toIntOrNull()

                        if (consumed != null && goal != null) {
                            showCaloriesLeft = true
                        } else {
                            // error message
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter valid numbers for calories consumed and goal.")
                            }
                        }
                    },
                    modifier = Modifier
                        .height(Dimensions.scaledDp(Dimensions.buttonHeight))
                        .width(Dimensions.scaledDp(Dimensions.buttonWidth))
                        .padding(Dimensions.scaledDp(Dimensions.buttonPadding))
                ) {
                    Text(
                        "Save",
                        style = buttonTextStyle,
                        modifier = Modifier.wrapContentSize(Alignment.Center)
                    )
                }
            }

            if (showCaloriesLeft) {
                val consumed = caloriesConsumed.toIntOrNull()
                val goal = calorieGoal.toIntOrNull()

                if (consumed != null && goal != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calories Left: ${goal - consumed} cal",
                            style = bodyTextStyle,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Invalid input for calories",
                        style = bodyTextStyle,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Description
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Description",
                    modifier = Modifier.weight(1f),
                    style = bodyTextStyle
                )
                // Description input
                BasicTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        hasChanges = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .width(250.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (description.isEmpty()) {
                                Text(
                                    "Enter a description (optional)",
                                    style = placeholderTextStyle
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = bodyTextStyle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            WeightGraph(sharedViewModel.savedGoals, sharedViewModel)
        }
    }
}

fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}

@Composable
fun WeightGraph(goals: List<Goal>, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val isDarkMode = sharedViewModel.isDarkMode
    val uniqueGoals = goals.distinctBy { it.date }

    if (uniqueGoals.isNotEmpty() && uniqueGoals.all { it.currentWeight > 0 }) {
        AndroidView(
            factory = {
                LineChart(context).apply {
                    description.isEnabled = false // disable desc label
                    setTouchEnabled(false) // disable touch gestures
                    setDrawGridBackground(false) // disable grid background
                    isDragEnabled = false // disable dragging
                    setScaleEnabled(true) // enable scaling
                    setPinchZoom(false) // disable pinch zoom
                    axisRight.isEnabled = false // turn off right y-axis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM // just have bottom x-axis
                    legend.isEnabled = false // disable legend of line color
                    xAxis.setLabelCount(uniqueGoals.size, true) // show only as many labels as data points
                    xAxis.granularity = 1f

                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < uniqueGoals.size) {
                                val date = uniqueGoals[index].date
                                val dateFormat = SimpleDateFormat("M-dd", Locale.getDefault())
                                val parsedDate = SimpleDateFormat("M-dd-yyyy", Locale.getDefault()).parse(date)

                                if (parsedDate != null) {
                                    dateFormat.format(parsedDate)
                                } else {
                                    ""
                                }
                            } else {
                                ""
                            }
                        }
                    }

                    setGraphColors(isDarkMode)
                    updateGraphData(uniqueGoals)
                }
            },
            update = { lineChart ->
                lineChart.updateGraphData(uniqueGoals)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } else {
        Text("No valid data to display", modifier = Modifier.padding(16.dp), fontSize = Dimensions.scaledSp(Dimensions.bodyTextSize))
    }
}

private fun LineChart.updateGraphData(goals: List<Goal>) {
    val entries = goals.mapIndexed { index, goal ->
        Entry(index.toFloat(), goal.currentWeight.toFloat())
    }
    if (entries.isNotEmpty()) {
        val dataSet = LineDataSet(entries, "").apply {
            color = Color.BLUE // line color
            setCircleColor(Color.BLUE) // point color
            lineWidth = 2f // line width
            circleRadius = 3f // circle radius
            setDrawCircleHole(false) // disable circle hole
            setDrawValues(false)
        }
        val lineData = LineData(dataSet)
        this.data = lineData
        invalidate()
    }
}

private fun LineChart.setGraphColors(isDarkMode: Boolean) {
    val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
    val gridColor = if (isDarkMode) Color.LTGRAY else Color.DKGRAY

    xAxis.textColor = textColor
    axisLeft.textColor = textColor
    axisRight.textColor = textColor

    xAxis.gridColor = gridColor
    axisLeft.gridColor = gridColor
    axisRight.gridColor = gridColor

    legend.textColor = textColor

    invalidate()
}