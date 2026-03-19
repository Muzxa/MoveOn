package com.example.moveon.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.PlaceholderFutureScreen
import com.example.moveon.ui.Screen
import com.example.moveon.ui.features.auth.AuthViewModel
import com.example.moveon.ui.features.auth.LoginScreen
import com.example.moveon.ui.features.auth.RegisterScreen
import com.example.moveon.ui.features.home.HomeScreen
import com.example.moveon.ui.features.inventory.InventoryScreen
import com.example.moveon.ui.features.onboarding.OnboardingScreen
import com.example.moveon.ui.features.profile.ProfileScreen
import com.example.moveon.ui.features.splash.SplashScreen
import com.moveon.app.ui.theme.MoveOnTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoveOnTheme {
                val navController = rememberNavController()

                val onTabSelected: (DashboardTab) -> Unit = { tab ->
                    val route = tab.route
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Splash Route
                    composable(Screen.Splash.route) {
                        val authViewModel: AuthViewModel = hiltViewModel()

                        SplashScreen(
                            onResolveSession = {
                                val targetRoute = if (authViewModel.isUserLoggedIn()) {
                                    Screen.Home.route
                                } else {
                                    Screen.Onboarding.route
                                }

                                navController.navigate(targetRoute) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Onboarding Route
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onGetStarted = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. Login Route
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onNavigateToHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            }
                        )
                    }

                    // 4. Register Route
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            onNavigateToHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 5. Home Route
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onTabSelected = onTabSelected,
                            onManageInventoryClick = {
                                onTabSelected(DashboardTab.Inventory)
                            }
                        )
                    }

                    // 6. Book Route (placeholder for future implementation)
                    composable(Screen.Book.route) {
                        BottomTabPlaceholderScreen(
                            title = "Book",
                            selectedTab = DashboardTab.Book,
                            onTabSelected = onTabSelected
                        )
                    }

                    // 7. Inventory Route
                    composable(Screen.Inventory.route) {
                        InventoryScreen(
                            onTabSelected = onTabSelected
                        )
                    }

                    // 8. Profile Route
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onTabSelected = onTabSelected,
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomTabPlaceholderScreen(
    title: String,
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            MoveOnBottomBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PlaceholderFutureScreen(
                title = title,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}