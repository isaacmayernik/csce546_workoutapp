package com.example.workoutapp546

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workoutapp546.ui.theme.WorkoutApp546Theme

class MainActivity : ComponentActivity() {
    private val sharedViewModel : SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutApp546Theme {
                NavigationApp(sharedViewModel)
            }
        }
    }
}

@Composable
fun NavigationApp(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()
    Scaffold (
        bottomBar = { BottomBarNavigation(navController) }
    ) { innerPadding ->
        NavigationGraph(navController, Modifier.padding(innerPadding), sharedViewModel)
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object WorkoutLog : Screen("workout_log", "Workout Log", Icons.Default.Home)
    data object Goals : Screen("goals", "Goals", Icons.AutoMirrored.Filled.List)
}

@Composable
fun BottomBarNavigation(navController: NavHostController) {
    val items = listOf (
        Screen.WorkoutLog,
        Screen.Goals,
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier, sharedViewModel: SharedViewModel) {
    NavHost(
        navController = navController,
        startDestination = Screen.WorkoutLog.route,
        modifier = modifier,
    ) {
        composable(Screen.WorkoutLog.route) { LogScreen(navController, sharedViewModel) }
        composable(Screen.Goals.route) { GoalsScreen(navController, sharedViewModel) }
    }
}

@Composable
fun LogScreen(navController: NavHostController, sharedViewModel: SharedViewModel) {
    WorkoutLogApp(sharedViewModel)
}

@Composable
fun GoalsScreen(navController: NavHostController, sharedViewModel: SharedViewModel) {
    Goals(sharedViewModel)
}

