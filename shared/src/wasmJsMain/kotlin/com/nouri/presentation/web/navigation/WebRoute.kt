package com.nouri.presentation.web.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.ui.graphics.vector.ImageVector

enum class WebRoute(
    val label: String,
    val icon: ImageVector,
) {
    Dashboard("Dashboard", Icons.Default.Home),
    MealPlan("Meal Plan", Icons.Default.TableRows),
    Progress("Progress", Icons.Default.ShowChart),
    Appointments("Appointments", Icons.Default.CalendarMonth),
    Settings("Settings", Icons.Default.Settings),
}
