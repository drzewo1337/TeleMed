package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.AppDatabaseProvider
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.notifications.ReminderScheduler
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.measurement.AddEditMeasurementScreen
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.settings.SettingsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val database = AppDatabaseProvider.getDatabase(applicationContext)
                val measurementRepository = MeasurementRepository(database.measurementDao())
                val settingsRepository = SettingsRepository(database.userSettingsDao())
                val homeViewModel = androidx.compose.runtime.remember {
                    HomeViewModel(measurementRepository)
                }

                ReminderScheduler.scheduleDailyReminders(applicationContext)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                navController = navController,
                                viewModel = homeViewModel
                            )
                        }
                        composable(Screen.AddMeasurement.route) {
                            AddEditMeasurementScreen(
                                navController = navController,
                                measurementRepository = measurementRepository,
                                settingsRepository = settingsRepository,
                                measurementId = null
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                settingsRepository = settingsRepository
                            )
                        }
                    }
                }
            }
        }
    }
}