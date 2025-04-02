package com.example.workoutapp546

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import kotlin.collections.flatten

data class AnimationState(
    val isAnimating: Boolean = false,
    val currentMuscle: String? = null,
    val currentColor: Color = Color(0xFF18CB65)
)


// define color level based on number of sets
fun getMuscleColor(sets: Int): Color {
    return when (sets) {
        0 -> Color(0xFF18CB65)
        1 -> Color(0xFFA8E02A)
        2 -> Color(0xFFFFFF2D)
        3 -> Color(0xFFFFD21F)
        4 -> Color(0xFFFFB30A)
        5 -> Color(0xFFCE3135)
        else -> Color(0xFFCE3135)
    }
}

// maps workout to muscle groups in muscle_groups.xml
val workoutMuscleMap = mapOf(
    "Barbell curls" to Pair(
        listOf("bicep"),
        "Keep your elbows close to your body. Control the movement all the way to below your neck."
    ),
    "Barbell raises" to Pair(
        listOf("shoulder-deltoid"),
        "Make sure your chest is in front of you with your core tight. Lift to eye-level and control the push down."
    ),
    "Bench press" to Pair(
        listOf("chest-left", "chest-right"),
        "Use a wider than shoulder-width grip. Shoulders should be pinned to the bench with your chest up and out."
    ),
    "Bicycle crunches" to Pair(
        listOf("ab", "oblique"),
        "Do not lock your fingers. Focus on twisting your torso. Controlled movements are important and will engage your core more."
    ),
    "Cable chest fly" to Pair(
        listOf("chest-left", "chest-right"),
        "Focus on a narrower range of motion. Fully contract the pec by bringing your hand past your center."
    ),
    "Cable lat pulldown" to Pair(
        listOf("trapezius", "back-lower", "bicep"),
        "Avoid leaning too far back. Squeeze your shoulder blades together."
    ),
    "Cable row" to Pair(
        listOf("trapezius", "back-lower", "bicep"),
        "Lead the pulling with your elbow, rather than your hands. Lock every finger around the grip to engage your back the most."
    ),
    "Cable tricep pushdown" to Pair(
        listOf("tricep", "shoulder-deltoid"),
        "Lean slightly forward. Keep bar slightly below chest. You want to use your shoulders as little as possible."
    ),
    "Calf raises" to Pair(
        listOf("calf"),
        "Keep repetitions slow and controlled."
    ),
    "Chest press machine" to Pair(
        listOf("chest-left", "chest-right"),
        "Lock every finger around the handle. Keep back straight the entire time. Controlled movements are extremely important, so consider lowering the weight if necessary."
    ),
    "Crunches" to Pair(
        listOf("ab"),
        "Squeeze your abs. Avoid locking your fingers and straining your neck. Controlled movements are important."
    ),
    "Dips" to Pair(
        listOf("chest-left", "chest-right"),
        "Maintain a neutral spine with your shoulders down. Tuck your elbows close to your body."
    ),
    "Dumbbell curls" to Pair(
        listOf("bicep"),
        "Avoid swinging or using momentum. Bring to above chest level. Maintain a straight back and engaged core."
    ),
    "Dumbbell front raises" to Pair(
        listOf("shoulder-deltoid"),
        "Keep palms faced downwards."
    ),
    "Dumbbell lateral raises" to Pair(
        listOf("shoulder-deltoid"),
        "Go no higher than shoulder height and bring it down with controlled movement."
    ),
    "Hammer curl" to Pair(
        listOf("pronator teres", "bicep", "forearm"),
        "Hold dumbbells with palms facing each other. Maintain a straight back and engaged core."
    ),
    "Hip thrust abduction" to Pair(
        listOf("glute", "hip"),
        "Squeeze your abs and glutes when thrusting up."
    ),
    "Incline chest press" to Pair(
        listOf("chest-right", "chest-left", "shoulder-deltoid"),
        "Lock every finger around the handle. Keep back straight the entire time."
    ),
    "Incline dumbbell press" to Pair(
        listOf("chest-right", "chest-left", "shoulder-deltoid"),
        "Lock every finger around the handle. Keep back straight the entire time."
    ),
    "Jump rope" to Pair(
        listOf("calf"),
        "Focus on wrist rotation. Keep your core engaged."
    ),
    "Lat pulldown" to Pair(
        listOf("trapezius", "back-lower", "bicep"),
        "Avoid leaning too far back. Squeeze your shoulder blades together."
    ),
    "Lateral raise machine" to Pair(
        listOf("shoulder-deltoid"),
        "Go no higher than shoulder height and bring it down with controlled movement."
    ),
    "Leg curls" to Pair(
        listOf("hamstring", "thigh"),
        "Maintain a straight back and engaged core. Focus on controlled movements. Start with a lighter weight."
    ),
    "Leg extension" to Pair(
        listOf("rectus-femoris", "vastus-medialis", "vastus-lateralis"),
        "Keep knees aligned with the leg bar’s pivot point. Focus on controlled movements."
    ),
    "Leg press" to Pair(
        listOf("vastus-medialis", "vastus-lateralis", "rectus-femoris"),
        "Safety is important here. Push the platform away with your heels and forefoot. Maintain a straight back and engaged core."
    ),
    "Lunge" to Pair(
        listOf("glute", "hip"),
        "Maintain an upright posture and avoid leaning forward or backward."
    ),
    "Mountain climbers" to Pair(
        listOf("rectus-abdominus", "oblique"),
        "Squeeze your abs when pushing your leg upwards."
    ),
    "Neck extension" to Pair(
        listOf("sternocleidomastoid"),
        ""
    ),
    "Overhead dumbbell extension" to Pair(
        listOf("tricep"),
        "Make a triangle with your hands under the top part of the dumbbell. Push outwards with it just behind your head."
    ),
    "Pectoral fly" to Pair(
        listOf("chest-left", "chest-right"),
        "Sit up straight. Lock every finger around the handle. Squeeze chest when reaching the center in front of you."
    ),
    "Plank" to Pair(
        listOf("ab", "rectus-abdominus", "oblique"),
        "Keep core engaged and abs squeezed the entire time. Maintain a flat back, hills and valleys are cheating."
    ),
    "Preacher curls" to Pair(
        listOf("bicep"),
        "Safety is important here. Keep back straight the entire time. Drive elbows firmly into the pad. Controlled movements are important here."
    ),
    "Pull-ups" to Pair(
        listOf("trapezius", "back-lower", "bicep", "deltoids-rear"),
        "Lead with your chest and keep your shoulders back."
    ),
    "Rear deltoid fly" to Pair(
        listOf("deltoids-rear"),
        "Squeeze your back when reaching the center in behind you. Controlled movements are important."
    ),
    "Reverse crunch" to Pair(
        listOf("rectus-abdominus", "ab"),
        "Roll pelvis upward rather than just lifting legs. Keep lower back pressed to floor."
    ),
    "Row machine" to Pair(
        listOf("trapezius", "back-lower", "bicep"),
        "Lead the pulling with your elbow, rather than your hands. Lock every finger around the grip to engage your back the most."
    ),
    "Russian twists" to Pair(
        listOf("oblique"),
        "Rotate from torso, not just arms. Keep feet elevated for added difficulty."
    ),
    "Seated military press" to Pair(
        listOf("shoulder-deltoid"),
        "Keep back against pad. Don't lock elbows at top. Lower bar to chin level."
    ),
    "Shoulder press machine" to Pair(
        listOf("shoulder-deltoid"),
        "Press upward without arching back."
    ),
    "Side plank" to Pair(
        listOf("glute", "hip", "oblique"),
        "Stack feet and keep body in straight line. Lift hips high to engage obliques."
    ),
    "Sit-ups" to Pair(
        listOf("ab"),
        "Place hands lightly behind ears, don't pull on neck."
    ),
    "Squat" to Pair(
        listOf("glute", "vastus-medialis", "vastus-lateralis", "rectus-femoris"),
        "Keep chest up, knees tracking over toes. Lower until thighs are parallel to floor."
    ),
    "Tricep dip" to Pair(
        listOf("shoulder-deltoid", "tricep"),
        "You want to use your shoulders as little as possible. Focus on controlled movements bringing it back."
    ),
    "Tricep pushdown" to Pair(
        listOf("tricep"),
        "Lean slightly forward. You want to use your shoulders as little as possible."
    ),
    "Wide glute bridge" to Pair(
        listOf("glute"),
        "Press through heels, squeeze glutes at top. Keep knees aligned with toes."
    ),
    "Wood chop" to Pair(
        listOf("oblique"),
        "Rotate through torso, not just arms. Keep movements controlled in both directions."
    ),
    "Wrist curls" to Pair(
        listOf("forearm"),
        "Use light dumbbell for higher rep counts. Do a slow tempo to maximize tension."
    )
)

fun saveMuscleState(sharedPreferences: SharedPreferences, date: String, muscleStates: Map<String, Color>) {
    val gson = Gson()
    val colorMap = muscleStates.mapValues { it.value.toArgb() }
    val json = gson.toJson(colorMap)
    sharedPreferences.edit { putString("muscle_state_$date", json) }
}

fun loadMuscleState(sharedPreferences: SharedPreferences, date: String): MutableMap<String, Color> {
    val gson = Gson()
    val json = sharedPreferences.getString("muscle_state_$date", null)
    val muscleStates = mutableStateMapOf<String, Color>()

    val allMuscles = workoutMuscleMap.values.map { it.first }.flatten().toSet()
    allMuscles.forEach { muscle ->
        muscleStates[muscle] = Color(0xFF18CB65)
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

fun getColorTransition(currentSets: Int, targetSets: Int): List<Color> {
    return (currentSets..targetSets).map { getMuscleColor(it) }
}