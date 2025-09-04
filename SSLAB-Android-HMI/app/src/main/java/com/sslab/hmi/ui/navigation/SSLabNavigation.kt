package com.sslab.hmi.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sslab.hmi.ui.screens.device.DeviceDiscoveryScreen
import com.sslab.hmi.ui.screens.environment.EnvironmentScreen
import com.sslab.hmi.ui.screens.home.HomeScreen
import com.sslab.hmi.ui.screens.interactive.InteractiveTeachingScreen
import com.sslab.hmi.ui.screens.power.TeachingPowerScreen
import com.sslab.hmi.ui.screens.settings.SettingsScreen
import com.sslab.hmi.ui.screen.ClassroomConfigScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SSLabNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = bottomNavItems.find { 
                            currentDestination?.hierarchy?.any { dest -> 
                                dest.route == it.route 
                            } == true 
                        }?.title ?: "SSLAB实验室环境控制系统"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTeachingPower = {
                        navController.navigate(Screen.TeachingPower.route)
                    },
                    onNavigateToEnvironment = {
                        navController.navigate(Screen.Environment.route)
                    },
                    onNavigateToInteractiveTeaching = {
                        navController.navigate(Screen.InteractiveTeaching.route)
                    },
                    onNavigateToDeviceDiscovery = {
                        navController.navigate(Screen.DeviceDiscovery.route)
                    },
                    onNavigateToClassroomConfig = {
                        navController.navigate(Screen.ClassroomConfig.route)
                    }
                )
            }
            
            composable(Screen.TeachingPower.route) {
                TeachingPowerScreen()
            }
            
            composable(Screen.Environment.route) {
                EnvironmentScreen()
            }
            
            composable(Screen.InteractiveTeaching.route) {
                InteractiveTeachingScreen()
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            
            composable(Screen.DeviceDiscovery.route) {
                DeviceDiscoveryScreen()
            }
            
            composable(Screen.ClassroomConfig.route) {
                ClassroomConfigScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
