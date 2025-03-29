package com.example.workoutapp546

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import coil.compose.AsyncImage
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

// Map muscle names to their respective drawable resources
val muscleDrawableMap = mapOf(
    "ab" to R.drawable.abs,
    "back-lower" to R.drawable.back_lower,
    "bicep" to R.drawable.bicep,
    "calf" to R.drawable.calves,
    "chest-right" to R.drawable.chest_right,
    "chest-left" to R.drawable.chest_left,
    "deltoids-rear" to R.drawable.deltoids_rear,
    "forearm" to R.drawable.forearms,
    "glute" to R.drawable.glutes,
    "hamstring" to R.drawable.hamstring,
    "hip" to R.drawable.hip,
    "oblique" to R.drawable.oblique,
    "pronator teres" to R.drawable.pronatur_teres,
    "rectus-abdominus" to R.drawable.rectus_abdominus,
    "rectus-femoris" to R.drawable.rectus_femoris,
    "shoulder-deltoid" to R. drawable.shoulder_deltoids,
    "sternocleidomastoid" to R.drawable.sternocleidomastoid,
    "thigh" to R.drawable.thigh,
    "trapezius" to R.drawable.trapezius,
    "tricep" to R.drawable.tricep,
    "vastus-lateralis" to R.drawable.vastus_lateralis,
    "vastus-medialis" to R.drawable.vastus_medialis,
)

// alternate names for workouts in Select Workout dialog search box
val synonymsMap = mapOf(
    "Bicep curls" to listOf("Barbell curls", "Dumbbell curls"),
    "Cable fly" to "Cable chest fly",
    "Cable tricep pushdown" to "Tricep pushdown",
    "Chest fly" to "Pectoral fly",
    "Chest press" to "Bench press",
    "Chest dips" to "Dips",
    "Chin-ups" to "Pull-ups",
    "Concentration dumbbell curls" to "Preacher curls",
    "Forearm curls" to "Wrist curls",
    "Glute bridge" to "Wide glute bridge",
    "Hammer bicep curls" to "Hammer curl",
    "Incline bench press" to "Incline chest press",
    "Reverse sit ups" to "Reverse crunch",
    "Seated cable row" to "Cable row",
    "Seated dips" to "Tricep dip",
    "Skipping rope" to "Jump rope",
    "Standing calf raises" to "Calf raises",
    "Tricep pushdown" to "Cable tricep pushdown",
    "Walking lunges" to "Lunge"
)

@Composable
fun MuscleGroupsView(
    muscleStates: Map<String, Color>,
    animationState: AnimationState
) {
    muscleStates.forEach { (muscle, color) ->
        if (muscle != animationState.currentMuscle) {
            val drawableRes = muscleDrawableMap[muscle]
            if (drawableRes != null) {
                AsyncImage(
                    model = drawableRes,
                    contentDescription = muscle,
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(color)
                )
            }
        }
    }

    // Draw the currently animating muscle on top
    animationState.currentMuscle?.let { muscle ->
        val drawableRes = muscleDrawableMap[muscle]
        if (drawableRes != null) {
            AsyncImage(
                model = drawableRes,
                contentDescription = muscle,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(animationState.currentColor)
            )
        }
    }
}