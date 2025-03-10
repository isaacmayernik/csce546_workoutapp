package com.example.workoutapp546

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedViewModel : ViewModel() {
    var savedGoals by mutableStateOf<List<Goal>>(emptyList())
        private set

    var isDarkMode by mutableStateOf(false)
        private set

    var savedRoutines by mutableStateOf<List<Routine>>(emptyList())
        private set

    private var _hasUnsavedChanges = mutableStateOf(false)
    val hasUnsavedChanges: Boolean
        get() = _hasUnsavedChanges.value

    fun setUnsavedChanges(hasChanges: Boolean) {
        _hasUnsavedChanges.value = hasChanges
    }

    fun loadGoals(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = sharedPreferences.getString("goals", null)
        savedGoals = if (json != null) {
            val type = object : TypeToken<List<Goal>>() {}.type
            gson.fromJson<List<Goal>>(json, type).sortedBy { it.date }
        } else {
            emptyList()
        }
    }

    fun saveGoals(sharedPreferences: SharedPreferences, goals: List<Goal>) {
        val gson = Gson()
        val json = gson.toJson(goals)
        sharedPreferences.edit().putString("goals", json).apply()
        savedGoals = goals
    }

    fun updateCaloriesConsumed(sharedPreferences: SharedPreferences, date: String, calories: Int) {
        val updatedGoals = savedGoals.map { goal ->
            if (goal.date == date) {
                goal.copy(caloriesConsumed = goal.caloriesConsumed + calories)
            } else {
                goal
            }
        }
        saveGoals(sharedPreferences, updatedGoals)
    }

    fun toggleDarkMode(enabled: Boolean, sharedPreferences: SharedPreferences) {
        isDarkMode = enabled
        saveDarkModeState(sharedPreferences, enabled)
    }

    fun loadDarkModeState(sharedPreferences: SharedPreferences) {
        isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
    }

    private fun saveDarkModeState(sharedPreferences: SharedPreferences, enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun loadRoutines(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = sharedPreferences.getString("routines", null)
        savedRoutines = if (json != null) {
            val type = object : TypeToken<List<Routine>>() {}.type
            gson.fromJson<List<Routine>>(json, type)
        } else {
            emptyList()
        }
    }

    fun saveRoutines(sharedPreferences: SharedPreferences, routines: List<Routine>) {
        val gson = Gson()
        val json = gson.toJson(routines)
        sharedPreferences.edit().putString("routines", json).apply()
        savedRoutines = routines
    }

    fun addRoutine(sharedPreferences: SharedPreferences, routine: Routine) {
        val updatedRoutines = savedRoutines + routine
        saveRoutines(sharedPreferences, updatedRoutines)
    }
}