package com.example.workoutapp546

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
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
    LaunchedEffect(currentGoal) {
        if (currentGoal != null) {
            currentWeight = currentGoal.currentWeight.toString()
            weightGoal = currentGoal.weightGoal.toString()
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
        if (weight != null && current != null && calories != null) {
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
        delay(500)
        saveGoal()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (currentGoal != null) {
            Text(
                text = "Last Updated: ${currentGoal.lastUpdated}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Current weight
        Row (
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
                onValueChange = {
                    currentWeight = it
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
                        if (currentWeight.isEmpty()) {
                            Text("Enter current weight", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weight goal
        Row (
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
                onValueChange = {
                    weightGoal = it
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
                        if (weightGoal.isEmpty()) {
                            Text("Enter weight goal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
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
            val activityLevels = listOf("Very Active", "Active", "Lightly Active", "Not Very Active")
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
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (calorieGoal.isEmpty()) {
                            Text("Enter a daily calorie goal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
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
            // Calorie goal input
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
                            Text("Enter a description (optional)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("yyyy--MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}