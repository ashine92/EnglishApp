package com.example.englishapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.englishapp.ui.screens.home.HomeScreen
import com.example.englishapp.ui.screens.search.SearchScreen
import com.example.englishapp.ui.screens.test.TestScreen
import com.example.englishapp.ui.screens.vocabulary.VocabListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Ẩn bottom bar khi đang ở Test screen
    val shouldShowBottomBar = currentDestination?.route != Screen.Test.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                NavigationBarItem(
                    icon = { Text("🏠", fontSize = 24.sp) },
                    label = { Text("Trang chủ") },
                    selected = currentDestination?.hierarchy?.any {
                        it.route == Screen.Home.route
                    } == true,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            // Clear toàn bộ back stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Text("🔍", fontSize = 24.sp) },
                    label = { Text("Tra từ") },
                    selected = currentDestination?.hierarchy?.any {
                        it.route == Screen.Search.route
                    } == true,
                    onClick = {
                        navController.navigate(Screen.Search.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Text("📚", fontSize = 24.sp) },
                    label = { Text("Từ vựng") },
                    selected = currentDestination?.hierarchy?.any {
                        it.route == Screen.VocabList.route
                    } == true,
                    onClick = {
                        navController.navigate(Screen.VocabList.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Text("✏️", fontSize = 24.sp) },
                    label = { Text("Kiểm tra") },
                    selected = currentDestination?.hierarchy?.any {
                        it.route == Screen.Test.route
                    } == true,
                    onClick = {
                        navController.navigate(Screen.Test.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
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
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToVocabList = {
                        navController.navigate(Screen.VocabList.route)
                    },
                    onNavigateToTest = {
                        navController.navigate(Screen.Test.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen()
            }

            composable(Screen.VocabList.route) {
                VocabListScreen(
                    onVocabClick = { vocab ->
                        // Navigate to detail if needed
                    }
                )
            }

            composable(Screen.Test.route) {
                TestScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}