package com.example.workoutapp546

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class SharedViewModel : ViewModel() {
    var savedGoals by mutableStateOf<List<Goal>>(emptyList())
        private set

    var isDarkMode by mutableStateOf(false)
        private set

    var savedRoutines by mutableStateOf<List<Routine>>(emptyList())
        private set

    var routineJustCreated by mutableStateOf(false)
        private set

    fun setRoutineCreated() {
        routineJustCreated = true
    }

    fun resetRoutineCreated() {
        routineJustCreated = false
    }

    private var _hasUnsavedChanges = mutableStateOf(false)
    val hasUnsavedChanges: Boolean
        get() = _hasUnsavedChanges.value

    fun setUnsavedChanges(hasChanges: Boolean) {
        _hasUnsavedChanges.value = hasChanges
    }

    private var _hasChangesMap = mutableStateMapOf<String, Boolean>()
    val hasChangesMap: Map<String, Boolean>
        get() = _hasChangesMap

    fun setHasChanges(date: String, hasChanges: Boolean, sharedPreferences: SharedPreferences) {
        _hasChangesMap[date] = hasChanges
        saveChangesState(sharedPreferences)
    }

    fun saveChangesState(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = gson.toJson(_hasChangesMap)
        sharedPreferences.edit { putString("changes_state", json) }
    }

    fun loadChangesState(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = sharedPreferences.getString("changes_state", null)
        if (json != null) {
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            _hasChangesMap.clear()
            _hasChangesMap.putAll(gson.fromJson(json, type))
        }
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
        sharedPreferences.edit { putString("goals", json) }
        savedGoals = goals.distinctBy { it.date } // no duplicate dates
    }

    fun toggleDarkMode(enabled: Boolean, sharedPreferences: SharedPreferences) {
        isDarkMode = enabled
        saveDarkModeState(sharedPreferences, enabled)
    }

    fun loadDarkModeState(sharedPreferences: SharedPreferences) {
        isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
    }

    private fun saveDarkModeState(sharedPreferences: SharedPreferences, enabled: Boolean) {
        sharedPreferences.edit{ putBoolean("dark_mode", enabled) }
    }

    fun loadRoutines(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = sharedPreferences.getString("routines", null)
        savedRoutines = if (json != null) {
            val type = object : TypeToken<List<Routine>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun saveRoutines(sharedPreferences: SharedPreferences, routines: List<Routine>) {
        val gson = Gson()
        val json = gson.toJson(routines)
        sharedPreferences.edit { putString("routines", json) }
        savedRoutines = routines
    }

    fun addRoutine(sharedPreferences: SharedPreferences, routine: Routine) {
        val updatedRoutines = savedRoutines + routine
        saveRoutines(sharedPreferences, updatedRoutines)
    }

    fun removeRoutine(sharedPreferences: SharedPreferences, routine: Routine) {
        val updatedRoutines = savedRoutines.toMutableList().apply {
            removeAll { it.name == routine.name }
        }
        saveRoutines(sharedPreferences, updatedRoutines)
    }
}