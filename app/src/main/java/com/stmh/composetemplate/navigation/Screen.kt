package com.stmh.composetemplate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notes
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "홈",
        icon = Icons.Default.Home
    )

    data object Hospital : Screen(
        route = "hospital",
        title = "병원",
        icon = Icons.Default.LocalHospital
    )

    data object Measurement : Screen(
        route = "measurement",
        title = "측정",
        icon = Icons.Default.AddCircle
    )

    data object Records : Screen(
        route = "records",
        title = "나의기록",
        icon = Icons.Default.Notes
    )

    data object More : Screen(
        route = "more",
        title = "더보기",
        icon = Icons.Default.MoreHoriz
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Hospital,
    Screen.Measurement,
    Screen.Records,
    Screen.More
)
