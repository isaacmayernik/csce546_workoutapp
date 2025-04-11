package com.example.workoutapp546

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.workoutapp546.screens.Goal
import com.example.workoutapp546.screens.Routine
import com.example.workoutapp546.screens.Workout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PersonalRecord(val reps: Int, val weight: Float)

class SharedViewModel : ViewModel() {
    var savedGoals by mutableStateOf<List<Goal>>(emptyList())
        private set

    var isDarkMode by mutableStateOf(false)
        private set

    var savedRoutines by mutableStateOf<List<Routine>>(emptyList())
        private set

    private var _personalRecords = mutableStateMapOf<String, PersonalRecord>()
    val personalRecords: Map<String, PersonalRecord> get() = _personalRecords

    fun checkNewPR(workout: Workout): Boolean {
        val currentPR = _personalRecords[workout.name]

        val bestSet = workout.sets.maxByOrNull { set ->
            set.reps * (set.weight ?: 0f)
        } ?: return false

        bestSet.weight?.let { weight ->
            if (currentPR == null ||
                (bestSet.reps * weight) > (currentPR.reps * currentPR.weight)) {
                _personalRecords[workout.name] = PersonalRecord(bestSet.reps, weight)
                return true
            }
        }
        return false
    }

    fun savePR(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = gson.toJson(_personalRecords)
        sharedPreferences.edit { putString("personal_records", json) }
    }

    fun loadPR(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = sharedPreferences.getString("personal_records", null)
        if (json != null) {
            try {
                val newType = object : TypeToken<Map<String, PersonalRecord>>() {}.type
                _personalRecords.clear()
                _personalRecords.putAll(gson.fromJson(json, newType))
            } catch (e: Exception) {
                // fix old formatted ones
                val oldType = object : TypeToken<Map<String, Int>>() {}.type
                val oldRecords: Map<String, Int> = gson.fromJson(json, oldType)
                oldRecords.forEach { (name, volume) ->
                    _personalRecords[name] = PersonalRecord(1, volume.toFloat())
                }
                savePR(sharedPreferences)
            }
        }
    }

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