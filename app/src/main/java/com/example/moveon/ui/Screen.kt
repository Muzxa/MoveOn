package com.example.moveon.ui

sealed class Screen (val route: String) {
    object Login: Screen("login")
    object Register : Screen("register")
    object Home: Screen(route = "home")
    object Logistics : Screen("logistics")
    object Inventory : Screen("inventory")
    object Settings : Screen("settings")
}