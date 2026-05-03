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
    object ScanBox : Screen("scan_box")
    object AddItemCamera : Screen("add_item_camera/{boxUuid}/{boxId}") {
        fun createRoute(boxUuid: String, boxId: String): String {
            return "add_item_camera/$boxUuid/$boxId"
        }
    }
    object BoxItems : Screen("box_items/{boxUuid}/{scannedFromQr}") {
        fun createRoute(boxUuid: String, scannedFromQr: Boolean): String {
            return "box_items/$boxUuid/$scannedFromQr"
        }
    }
    object Profile : Screen("profile")
    object ProviderProfile : Screen("provider_profile")
    object EditProfile : Screen("edit_profile")
    object SavedAddresses : Screen("saved_addresses")
    object MoveHistory : Screen("move_history")
    object TrackBooking : Screen("track_booking/{bookingId}") {
        fun createRoute(bookingId: String): String = "track_booking/$bookingId"
    }
    object Settings : Screen("settings")
    object AppSettings : Screen("app_settings")
    object Security : Screen("security")
    object VerifyIdentity : Screen("verify_identity")
    object SecurityOtp : Screen("security_otp/{method}") {
        fun createRoute(method: String): String = "security_otp/$method"
    }
    object NewPassword : Screen("new_password")
    object SecurityUpdated : Screen("security_updated")
}