package com.example.moveon.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moveon.ui.Screen
import com.example.moveon.ui.features.auth.LoginScreen
import com.example.moveon.ui.features.auth.RegisterScreen
import com.example.moveon.ui.features.home.HomeScreen
import com.example.moveon.ui.features.onboarding.OnboardingScreen
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

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Splash Route
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigateToOnboarding = {
                                navController.navigate(Screen.Onboarding.route) {
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
                        HomeScreen()
                    }
                }
            }
        }
    }
}