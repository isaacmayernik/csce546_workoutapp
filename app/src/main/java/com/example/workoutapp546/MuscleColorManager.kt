package com.example.workoutapp546

import android.content.SharedPreferences
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// define color level based on number of sets
fun getMuscleColor(sets: Int): Int {
    return when {
        sets >= 3 -> Color.RED
        sets == 2 -> Color.rgb(255, 165, 0)
        sets == 1 -> Color.YELLOW
        else -> Color.GREEN
    }
}

// maps workout to muscle groups in muscle_groups.xml
val workoutMuscleMap = mapOf(
    "Dumbbell front raises" to listOf("shoulder-deltoid"),
    "Seated military press" to listOf("shoulder-deltoid"),
    "Preacher curls" to listOf("bicep"),
    "Barbell curls" to listOf("bicep"),
    "Dumbbell curls" to listOf("bicep"),
    "Wrist curls" to listOf("forearm"),
    "Russian twists" to listOf("oblique"),
    "Wood chop" to listOf("oblique"),
    "Mountain climbers" to listOf("rectus-abdominus", "oblique"),
    "Sit-ups" to listOf("ab"),
    "Plank" to listOf("ab", "rectus-abdominus", "oblique"),
    "Crunches" to listOf("ab"),
    "Bench press" to listOf("chest-left", "chest-right"),
    "Pectoral fly" to listOf("chest-left", "chest-right"),
    "Dips" to listOf("chest-left", "chest-right"),
    "Calf raises" to listOf("calf"),
    "Leg curls" to listOf("hamstring", "thigh"),
    "Leg extension" to listOf("rectus-femoris", "vastus-medialis", "vestus_medialis"),
    "Cable row" to listOf("trapezius", "back-lower", "bicep"),
    "Lat pulldown" to listOf("trapezius", "back-lower", "bicep"),
    "Tricep dip" to listOf("shoulder-deltoid", "tricep"),
    "Tricep pushdown" to listOf("tricep"),
    "Overhead dumbbell extension" to listOf("tricep"),
    "Barbell raises" to listOf("shoulder-deltoid"),
    "Hip thrust abduction" to listOf("glute", "hip"),
    "Wide glute bridge" to listOf("glute"),
    "Squat" to listOf("glute", "vastus-medialis", "vastus-lateralis", "rectus-femoris"),
    "Reverse crunch" to listOf("rectus-abdominus", "ab"),
    "Bicycle crunches" to listOf("ab", "oblique"),
    "Hammer curl" to listOf("pronator teres", "bicep", "forearm"),
    "Neck extension" to listOf("sternocleidomastoid"),
    "Jump rope" to listOf("calf"),
    "Leg press" to listOf("vastus-medialis", "vastus-lateralis", "rectus-femoris")
)

fun saveMuscleState(sharedPreferences: SharedPreferences, date: String, muscleStates: Map<String, Int>) {
    val gson = Gson()
    val json = gson.toJson(muscleStates)
    sharedPreferences.edit().putString("muscle_state_$date", json).apply()
}

fun loadMuscleState(sharedPreferences: SharedPreferences, date: String): MutableMap<String, Int> {
    val gson = Gson()
    val json = sharedPreferences.getString("muscle_state_$date", null)
    return if (json != null) {
        val type = object : TypeToken<MutableMap<String, Int>>() {}.type
        gson.fromJson(json, type)
    } else {
        mutableMapOf()
    }
}