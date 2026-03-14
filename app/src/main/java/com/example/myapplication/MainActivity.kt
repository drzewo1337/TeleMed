package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.myapplication.data.local.AppDatabaseProvider
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.notifications.ReminderScheduler
import com.example.myapplication.ui.analysis.AnalysisScreen
import com.example.myapplication.ui.analysis.AnalysisViewModel
import com.example.myapplication.ui.calendar.CalendarScreen
import com.example.myapplication.ui.calendar.CalendarViewModel
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.measurement.AddEditMeasurementScreen
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.settings.SettingsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val database = AppDatabaseProvider.getDatabase(applicationContext)
                val measurementRepository = remember { MeasurementRepository(database.measurementDao()) }
                val settingsRepository = remember { SettingsRepository(database.userSettingsDao()) }
                val homeViewModel = remember { HomeViewModel(measurementRepository) }
                val calendarViewModel = remember { CalendarViewModel(measurementRepository) }
                val analysisViewModel = remember { AnalysisViewModel(measurementRepository, settingsRepository) }

                ReminderScheduler.scheduleDailyReminders(applicationContext)

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val mainRoutes = setOf(
                    Screen.Home.route,
                    Screen.Calendar.route,
                    Screen.Analysis.route,
                    Screen.Settings.route
                )
                val showBottomBar = currentRoute in mainRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                modifier = Modifier.height(80.dp)
                            ) {
                                listOf(
                                    Triple(Screen.Home.route, "Pomiary", Icons.Filled.Home),
                                    Triple(Screen.Calendar.route, "Kalendarz", Icons.Filled.CalendarMonth),
                                    Triple(Screen.Analysis.route, "Analiza", Icons.Filled.ShowChart),
                                    Triple(Screen.Settings.route, "Ustawienia", Icons.Filled.Settings)
                                ).forEach { (route, label, icon) ->
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        selected = currentRoute == route,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    navController = navController,
                                    viewModel = homeViewModel
                                )
                            }
                            composable(Screen.Calendar.route) {
                                CalendarScreen(
                                    navController = navController,
                                    viewModel = calendarViewModel
                                )
                            }
                            composable(Screen.Analysis.route) {
                                AnalysisScreen(viewModel = analysisViewModel)
                            }
                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    navController = navController,
                                    settingsRepository = settingsRepository
                                )
                            }
                            composable(
                                route = Screen.AddMeasurement.route,
                                arguments = listOf(
                                    navArgument("selectedDate") {
                                        defaultValue = LocalDate.now().toString()
                                        type = NavType.StringType
                                    }
                                )
                            ) { backStackEntry ->
                                val selectedDateStr = backStackEntry.arguments?.getString("selectedDate") ?: LocalDate.now().toString()
                                AddEditMeasurementScreen(
                                    navController = navController,
                                    measurementRepository = measurementRepository,
                                    settingsRepository = settingsRepository,
                                    measurementId = null,
                                    initialSelectedDate = selectedDateStr
                                )
                            }
                            composable(
                                route = Screen.EditMeasurement.route,
                                arguments = listOf(navArgument("measurementId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val measurementId = backStackEntry.arguments?.getLong("measurementId") ?: 0L
                                AddEditMeasurementScreen(
                                    navController = navController,
                                    measurementRepository = measurementRepository,
                                    settingsRepository = settingsRepository,
                                    measurementId = measurementId,
                                    initialSelectedDate = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
