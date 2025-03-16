package com.example.workoutapp546

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// define color level based on number of sets
fun getMuscleColor(sets: Int): Color {
    return when {
        sets >= 3 -> Color.Red
        sets == 2 -> Color(0xFFFFA500)
        sets == 1 -> Color.Yellow
        else -> Color(0xFF1ABA00)
    }
}

// maps workout to muscle groups in muscle_groups.xml
val workoutMuscleMap = mapOf(
    "Barbell curls" to listOf("bicep"),
    "Barbell raises" to listOf("shoulder-deltoid"),
    "Bench press" to listOf("chest-left", "chest-right"),
    "Bicycle crunches" to listOf("ab", "oblique"),
    "Cable chest fly" to listOf("chest-left", "chest-right"),
    "Cable lat pulldown" to listOf("trapezius"),
    "Cable row" to listOf("trapezius", "back-lower", "bicep"),
    "Cable tricep pushdown" to listOf("tricep", "shoulder-deltoid"),
    "Calf raises" to listOf("calf"),
    "Chest press machine" to listOf("chest-right", "chest-right"),
    "Crunches" to listOf("ab"),
    "Dips" to listOf("chest-left", "chest-right"),
    "Dumbbell curls" to listOf("bicep"),
    "Dumbbell front raises" to listOf("shoulder-deltoid"),
    "Dumbbell lateral raises" to listOf("shoulder-deltoid"),
    "Hammer curl" to listOf("pronator teres", "bicep", "forearm"),
    "Hip thrust abduction" to listOf("glute", "hip"),
    "Incline chest press" to listOf("chest-right", "chest-left", "shoulder-deltoid"),
    "Incline dumbbell press" to listOf("chest-right", "chest-left", "shoulder-deltoid"),
    "Jump rope" to listOf("calf"),
    "Lat pulldown" to listOf("trapezius", "back-lower", "bicep"),
    "Lateral raise machine" to listOf("shoulder-deltoid"),
    "Leg curls" to listOf("hamstring", "thigh"),
    "Leg extension" to listOf("rectus-femoris", "vastus-medialis", "vastus-lateralis"),
    "Leg press" to listOf("vastus-medialis", "vastus-lateralis", "rectus-femoris"),
    "Lunge" to listOf("glute", "hip"),
    "Mountain climbers" to listOf("rectus-abdominus", "oblique"),
    "Neck extension" to listOf("sternocleidomastoid"),
    "Overhead dumbbell extension" to listOf("tricep"),
    "Pectoral fly" to listOf("chest-left", "chest-right"),
    "Plank" to listOf("ab", "rectus-abdominus", "oblique"),
    "Preacher curls" to listOf("bicep"),
    "Pull-ups" to listOf("trapezius", "back-lower", "bicep", "deltoids-rear"),
    "Rear deltoid fly" to listOf("deltoids-rear"),
    "Reverse crunch" to listOf("rectus-abdominus", "ab"),
    "Row machine" to listOf("trapezius", "back-lower", "bicep"),
    "Russian twists" to listOf("oblique"),
    "Seated military press" to listOf("shoulder-deltoid"),
    "Shoulder press machine" to listOf("shoulder-deltoid"),
    "Side plank" to listOf("glute", "hip", "oblique"),
    "Sit-ups" to listOf("ab"),
    "Squat" to listOf("glute", "vastus-medialis", "vastus-lateralis", "rectus-femoris"),
    "Tricep dip" to listOf("shoulder-deltoid", "tricep"),
    "Tricep pushdown" to listOf("tricep"),
    "Wide glute bridge" to listOf("glute"),
    "Wood chop" to listOf("oblique"),
    "Wrist curls" to listOf("forearm")
)

fun saveMuscleState(sharedPreferences: SharedPreferences, date: String, muscleStates: Map<String, Color>) {
    val gson = Gson()
    val colorMap = muscleStates.mapValues { it.value.toArgb() }
    val json = gson.toJson(colorMap)
    sharedPreferences.edit().putString("muscle_state_$date", json).apply()
}

fun loadMuscleState(sharedPreferences: SharedPreferences, date: String): MutableMap<String, Color> {
    val gson = Gson()
    val json = sharedPreferences.getString("muscle_state_$date", null)
    val muscleStates = mutableStateMapOf<String, Color>()

    val allMuscles = workoutMuscleMap.values.flatten().toSet()
    allMuscles.forEach { muscle ->
        muscleStates[muscle] = Color(0xFF1ABA00)
    }

    if (json != null) {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        val savedStates: Map<String, Int> = gson.fromJson(json, type)
        savedStates.forEach { (muscle, colorInt) ->
            muscleStates[muscle] = Color(colorInt)
        }
    }

    return muscleStates
}