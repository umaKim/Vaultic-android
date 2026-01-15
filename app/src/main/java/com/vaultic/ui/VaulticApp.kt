package com.vaultic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private sealed class Tab(val route: String, val label: String) {
    data object Home : Tab("home", "Home")
    data object Send : Tab("send", "Send")
    data object Activity : Tab("activity", "Activity")
    data object Settings : Tab("settings", "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaulticApp(
    viewModel: VaulticViewModel,
    onRequireBiometric: (String, () -> Unit, (String) -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    var unlocked by remember { mutableStateOf(!state.appLockEnabled) }

    LaunchedEffect(state.appLockEnabled) {
        if (state.appLockEnabled && !unlocked) {
            onRequireBiometric(
                "Unlock Vaultic",
                { unlocked = true },
                { error ->
                    if (error == "Biometrics unavailable") {
                        viewModel.updateAppLockEnabled(false)
                        unlocked = true
                    } else {
                        unlocked = false
                    }
                }
            )
        }
    }

    val tabs = listOf(Tab.Home, Tab.Send, Tab.Activity, Tab.Settings)

    if (state.wallets.isEmpty()) {
        OnboardingScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(tab.label) },
                        icon = {}
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(navController = navController, startDestination = Tab.Home.route) {
                composable(Tab.Home.route) { HomeScreen(viewModel = viewModel) }
                composable(Tab.Send.route) { SendScreen(viewModel = viewModel) }
                composable(Tab.Activity.route) { ActivityScreen(viewModel = viewModel) }
                composable(Tab.Settings.route) {
                    SettingsScreen(viewModel = viewModel, onRequireBiometric = onRequireBiometric)
                }
            }
            if (state.appLockEnabled && !unlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000))
                )
            }
        }
    }
}
