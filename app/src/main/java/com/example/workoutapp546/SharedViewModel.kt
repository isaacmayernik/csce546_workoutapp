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
}