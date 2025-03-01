package com.example.workoutapp546

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class Goal(
    val weightGoal: Double,
    val currentWeight: Double,
    val activityLevel: String,
    val calorieGoal: Int,
    val caloriesConsumed: Int,
    val description: String,
    val date: String,
)

@Composable
fun Goals(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("GoalsApp", Context.MODE_PRIVATE) }

    var weightGoal by remember { mutableStateOf("") }
    var currentWeight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("") }
    var calorieGoal by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sharedViewModel.loadGoals(sharedPreferences)
    }
    val savedGoals = sharedViewModel.savedGoals

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Current weight input
        BasicTextField(
            value = currentWeight,
            onValueChange = { currentWeight = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (currentWeight.isEmpty()) {
                        Text("Enter current weight (ex: 109.7) in lbs", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            }
        )

        // Weight goal input
        BasicTextField(
            value = weightGoal,
            onValueChange = { weightGoal = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (weightGoal.isEmpty()) {
                        Text("Enter weight goal (ex: 145.4) in lbs", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            }
        )

        // Activity level input
        val activityLevels = listOf("Very Active", "Active", "Lightly Active", "Not Very Active")
        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
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
                        }
                    )
                }
            }
        }

        // Calorie goal input
        BasicTextField(
            value = calorieGoal,
            onValueChange = { calorieGoal = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (calorieGoal.isEmpty()) {
                        Text("Enter a daily calorie goal (ex: 3000)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            }
        )

        // Description input
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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

        // Save goals
        Button(
            onClick = {
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
                        date = currentDate
                    )

                    val updatedGoals = savedGoals.filter { it.date != currentDate } + goal
                    sharedViewModel.saveGoals(sharedPreferences, updatedGoals)

                    weightGoal = ""
                    currentWeight = ""
                    activityLevel = ""
                    calorieGoal = ""
                    description = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goal")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(savedGoals) { goal ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Current Weight: ${goal.currentWeight} lbs")
                    Text("Weight Goal: ${goal.weightGoal} lbs")
                    Text("Activity Level: ${goal.activityLevel}")
                    Text("Calorie Goal: ${goal.calorieGoal}")
                    if(goal.description.isNotBlank()) {
                        Text("Description: ${goal.description}")
                    }
                    Text("Date: ${goal.date}")
                }
            }
        }
    }
}