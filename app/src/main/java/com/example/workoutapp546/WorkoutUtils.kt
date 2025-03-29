package com.example.workoutapp546

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.workoutapp546.screens.Workout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

fun saveWorkouts(sharedPreferences: SharedPreferences, date: String, workouts: List<Workout>) {
    val muscleSets = mutableMapOf<String, Int>()

    workouts.forEach { workout ->
        val muscles = workoutMuscleMap[workout.name] ?: listOf()
        muscles.forEach { muscle ->
            muscleSets[muscle] = (muscleSets[muscle] ?: 0) + workout.sets.size
        }
    }

    val gson = Gson()
    sharedPreferences.edit {
        putString(date, gson.toJson(workouts))
        putString("${date}_muscle_sets", gson.toJson(muscleSets))
    }
}

fun loadWorkouts(sharedPreferences: SharedPreferences, date: String): Triple<List<Workout>, Map<String, Int>, Boolean> {
    val gson = Gson()
    val workouts = sharedPreferences.getString(date, null)?.let {
        val type = object : TypeToken<List<Workout>>() {}.type
        gson.fromJson<List<Workout>>(it, type) ?: emptyList()
    } ?: emptyList()

    val muscleSets = sharedPreferences.getString("${date}_muscle_sets", null)?.let {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        gson.fromJson<Map<String, Int>>(it, type) ?: emptyMap()
    } ?: emptyMap()

    return Triple(workouts, muscleSets, workouts.isNotEmpty())
}

fun getDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun formatDate(year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(year, month, day)
    }
    return SimpleDateFormat("M-dd-yyyy", Locale.getDefault()).format(calendar.time)
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getPreviousDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate)!!
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    return dateFormat.format(calendar.time)
}

fun getNextDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate)!!
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    return dateFormat.format(calendar.time)
}