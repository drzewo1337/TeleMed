package com.example.myapplication.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddMeasurement : Screen("add_measurement")
    data object Settings : Screen("settings")
}

