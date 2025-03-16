package com.example.workoutapp546

import android.content.Context
import android.graphics.Color
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
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
    var description by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }

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
            description = currentGoal.description
            hasChanges = false
        }
    }

    fun saveGoal() {
        val weight = weightGoal.toDoubleOrNull()
        val current = currentWeight.toDoubleOrNull()
        val calories = calorieGoal.toIntOrNull()
        if (weight != null && current != null && calories != null && activityLevel.isNotEmpty()) {
            val currentDate = getCurrentDate()
            val existingGoal = savedGoals.find { it.date == currentDate }
            val caloriesConsumed = existingGoal?.caloriesConsumed ?: 0
            val goal = Goal(
                weightGoal = weight,
                currentWeight = current,
                activityLevel = activityLevel,
                calorieGoal = calories,
                caloriesConsumed = caloriesConsumed,
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Enter in Goals!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            if (currentGoal != null) {
                Text(
                    text = "Last Updated: ${currentGoal.lastUpdated}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Weight",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weight goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal Weight",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activity level
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Level",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                // Activity level input
                val activityLevels =
                    listOf("Very Active", "Active", "Lightly Active", "Not Very Active")
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(activityLevel.ifEmpty { "Select activity level" })
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        activityLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Calorie goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calorie Goal",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Description",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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

    if (uniqueGoals.isNotEmpty()) {
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
                            return if (index < uniqueGoals.size) {
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
        Text("Please enter your current weight to view a graph!", modifier = Modifier.padding(16.dp))
    }
}

private fun LineChart.updateGraphData(goals: List<Goal>) {
    val entries = goals.mapIndexed { index, goal ->
        Entry(index.toFloat(), goal.currentWeight.toFloat())
    }
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