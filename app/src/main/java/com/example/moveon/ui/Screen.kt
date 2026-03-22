package com.example.moveon.ui

sealed class Screen (val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login: Screen("login")
    object SignUp : Screen("signup")
    object RoleChoose : Screen("role_choose")
    object ProviderSetupStepOne : Screen("provider_setup_step_1")
    object ProviderSetupStepTwo : Screen("provider_setup_step_2")
    object ProviderSetupStepThree : Screen("provider_setup_step_3")
    object ProviderDashboard : Screen("provider_dashboard")
    object Home: Screen(route = "home")
    object Book : Screen("book")
    object Logistics : Screen("logistics")
    object Inventory : Screen("inventory")
    object Profile : Screen("profile")
    object ProviderProfile : Screen("provider_profile")
    object Settings : Screen("settings")
}