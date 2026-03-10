package com.example.myapplication.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Charts : Screen("charts")
    data object Settings : Screen("settings")
    data object AddMeasurement : Screen("add_measurement/{selectedDate}") {
        fun createRoute(selectedDate: String) = "add_measurement/$selectedDate"
    }
    data object EditMeasurement : Screen("edit_measurement/{measurementId}") {
        fun createRoute(measurementId: Long) = "edit_measurement/$measurementId"
    }
}

