package com.example.workoutapp546

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workoutapp546.notifications.RequestNotificationPermissions
import com.example.workoutapp546.screens.CreateRoutine
import com.example.workoutapp546.screens.Goals
import com.example.workoutapp546.screens.Settings
import com.example.workoutapp546.screens.WorkoutLogApp
import com.example.workoutapp546.ui.theme.WorkoutApp546Theme

class MainActivity : ComponentActivity() {
    private val sharedViewModel : SharedViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedViewModel.loadDarkModeState(sharedPreferences)
        sharedViewModel.loadRoutines(sharedPreferences)

        setContent {
            WorkoutApp546Theme(
                sharedViewModel = sharedViewModel,
            ) {
                Surface {
                    RequestNotificationPermissions()
                    NavigationApp(sharedViewModel)
                }
            }
        }
    }
}

@Composable
fun NavigationApp(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()

    Scaffold (
        bottomBar = {
            BottomBarNavigation(navController, sharedViewModel)
        }
    ) { innerPadding ->
        NavigationGraph(navController, Modifier.padding(innerPadding), sharedViewModel)
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object WorkoutLog : Screen("workout_log", "Workout Log", Icons.Default.Home)
    data object Goals : Screen("goals", "Goals", Icons.AutoMirrored.Filled.List)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun BottomBarNavigation(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
) {
    val items = listOf(
        Screen.WorkoutLog,
        Screen.Goals,
        Screen.Settings,
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var targetRoute by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = sharedViewModel.hasUnsavedChanges) {
        showConfirmationDialog = true
        targetRoute = null
    }

    NavigationBar(
        modifier = Modifier.height(90.dp)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    val isOnCreateRoutine = currentRoute == "create_routine"
                    val hasUnsavedChanges = sharedViewModel.hasUnsavedChanges

                    if (isOnCreateRoutine && hasUnsavedChanges) {
                        showConfirmationDialog = true
                        targetRoute = screen.route
                    } else {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                                inclusive = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        screen.title,
                        fontSize = 8.sp
                    )
                }
            )
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure?")},
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        targetRoute?.let { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                    inclusive = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } ?: run {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier, sharedViewModel: SharedViewModel) {
    NavHost(
        navController = navController,
        startDestination = Screen.WorkoutLog.route,
        modifier = modifier,
    ) {
        composable(Screen.WorkoutLog.route) { LogScreen(sharedViewModel) }
        composable(Screen.Goals.route) { GoalsScreen(navController, sharedViewModel) }
        composable(Screen.Settings.route) { SettingsScreen(navController, sharedViewModel) }
        composable("create_routine") { CreateRoutine(sharedViewModel, navController) }
    }
}

@Composable
fun LogScreen(sharedViewModel: SharedViewModel) {
    WorkoutLogApp(sharedViewModel)
}

@Composable
fun GoalsScreen(navController: NavHostController, sharedViewModel: SharedViewModel) {
    Goals(sharedViewModel, navController)
}

@Composable
fun SettingsScreen(navController: NavHostController, sharedViewModel: SharedViewModel) {
    Settings(sharedViewModel, navController)
}

