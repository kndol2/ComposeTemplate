package com.stmh.composetemplate.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.stmh.composetemplate.data.preferences.AuthPreferences
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authPreferences: AuthPreferences,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)

        if (authPreferences.isLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ComposeTemplate",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
