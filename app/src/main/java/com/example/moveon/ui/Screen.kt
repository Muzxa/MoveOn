package com.example.moveon.ui

sealed class Screen (val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login: Screen("login")
    object Register : Screen("register")
    object Home: Screen(route = "home")
    object Book : Screen("book")
    object Logistics : Screen("logistics")
    object Inventory : Screen("inventory")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}