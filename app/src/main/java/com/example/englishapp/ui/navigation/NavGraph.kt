package com.example.englishapp.ui.navigation

import androidx.compose.foundation.layout.padding
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.englishapp.data.repository.FlashcardRepository
import com.example.englishapp.ui.screens.flashcard.*
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
    
    // Ẩn bottom bar khi ở Test screen hoặc Flashcard study screen
    val shouldShowBottomBar = currentDestination?.route != Screen.Test.route &&
            currentDestination?.route?.startsWith("flashcard_study") != true

    var showCreateDeckDialog by remember { mutableStateOf(false) }

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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
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
                        icon = { Text("🎴", fontSize = 24.sp) },
                        label = { Text("Flashcard") },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == Screen.FlashcardDeckList.route ||
                                    it.route?.startsWith("flashcard_deck_detail") == true ||
                                    it.route?.startsWith("add_card") == true
                        } == true,
                        onClick = {
                            navController.navigate(Screen.FlashcardDeckList.route) {
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

            // Flashcard Deck List
            composable(Screen.FlashcardDeckList.route) {
                FlashcardDeckListScreen(
                    onDeckClick = { deckId ->
                        navController.navigate(Screen.FlashcardDeckDetail.createRoute(deckId))
                    },
                    onCreateDeck = {
                        showCreateDeckDialog = true
                    }
                )
            }

            // Flashcard Deck Detail
            composable(
                route = Screen.FlashcardDeckDetail.route,
                arguments = listOf(
                    navArgument("deckId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
                val flashcardRepository: FlashcardRepository = koinInject()

                FlashcardDeckDetailScreen(
                    deckId = deckId,
                    flashcardRepository = flashcardRepository,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onStartStudy = { id ->
                        navController.navigate(Screen.FlashcardStudy.createRoute(id))
                    },
                    onAddCard = {
                        navController.navigate(Screen.AddCard.createRoute(deckId))
                    }
                )
            }

            // Add Card
            composable(
                route = Screen.AddCard.route,
                arguments = listOf(
                    navArgument("deckId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
                val flashcardRepository: FlashcardRepository = koinInject()

                AddCardScreen(
                    deckId = deckId,
                    flashcardRepository = flashcardRepository,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onCardAdded = {
                        navController.popBackStack()
                    }
                )
            }

            // Flashcard Study Screen
            composable(
                route = Screen.FlashcardStudy.route,
                arguments = listOf(
                    navArgument("deckId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L

                FlashcardStudyScreen(
                    deckId = deckId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Create Deck Dialog
        if (showCreateDeckDialog) {
            val viewModel: FlashcardViewModel = koinInject()
            CreateDeckDialog(
                onDismiss = { showCreateDeckDialog = false },
                onCreate = { name, description ->
                    viewModel.createDeck(name, description)
                    showCreateDeckDialog = false
                }
            )
        }
    }
}