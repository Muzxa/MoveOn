package com.example.moveon.ui.features.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.data.local.dao.UserPreferences
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    suspend fun reserveAccount(email: String, pass: String): Result<Unit> =
        authRepository.reserveAccount(email, pass)

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    private val _rememberedEmail = mutableStateOf("")
    val rememberedEmail: State<String> = _rememberedEmail

    private val _rememberMeEnabled = mutableStateOf(false)
    val rememberMeEnabled: State<Boolean> = _rememberMeEnabled

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            val enabled = userPreferences.isRememberMeEnabled()
            _rememberMeEnabled.value = enabled
            _rememberedEmail.value = if (enabled) {
                userPreferences.getRememberedEmail().orEmpty()
            } else {
                ""
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Login -> login(event.email, event.password, event.rememberMe)
            is AuthEvent.GoogleSignIn -> signInWithGoogle(event.idToken)
            is AuthEvent.RegisterUser -> registerUser(event.email, event.password, event.fName, event.lName, event.pNumber)
            is AuthEvent.RegisterProvider -> registerProvider(
                event.email, event.password, event.fName, event.lName, event.pNumber,
                event.establishmentName, event.baseRate, event.ratePerKm,
                event.businessLat, event.businessLng
            )
            is AuthEvent.RegisterDriver -> registerDriver(
                event.email, event.password, event.fName, event.lName, event.pNumber,
                event.providerId, event.vehicleId, event.licenseNo
            )
            is AuthEvent.Logout -> logout()
        }
    }

    private fun login(email: String, pass: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, pass)
                .onSuccess {
                    if (rememberMe) {
                        userPreferences.setRememberMeEnabled(true)
                        userPreferences.setRememberedEmail(email)
                    } else {
                        userPreferences.clearRememberMeData()
                    }

                    _rememberMeEnabled.value = rememberMe
                    _rememberedEmail.value = if (rememberMe) email else ""
                    _authState.value = AuthState.Success
                    _eventFlow.emit(UiEvent.NavigateToHome(it.role))
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                }
        }
    }

    private fun registerUser(email: String, pass: String, fName: String, lName: String, pNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.registerUser(email, pass, fName, lName, pNumber)
                .onSuccess {
                    _authState.value = AuthState.Success
                    _eventFlow.emit(UiEvent.NavigateToHome(it.role))
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
        }
    }

    private fun registerProvider(
        email: String, pass: String, fName: String, lName: String, pNumber: String,
        establishmentName: String,
        baseRate: Double,
        ratePerKm: Double,
        businessLat: Double,
        businessLng: Double
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.registerProvider(
                email = email,
                pass = pass,
                fName = fName,
                lName = lName,
                pNumber = pNumber,
                establishmentName = establishmentName,
                baseRate = baseRate,
                ratePerKm = ratePerKm,
                businessLat = businessLat,
                businessLng = businessLng
            )
                .onSuccess {
                    _authState.value = AuthState.Success
                    _eventFlow.emit(UiEvent.NavigateToHome(it.role))
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Provider registration failed")
                }
        }
    }

    private fun registerDriver(
        email: String, pass: String, fName: String, lName: String, pNumber: String,
        providerId: String, vehicleId: String, licenseNo: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.registerDriver(email, pass, fName, lName, pNumber, providerId, vehicleId, licenseNo)
                .onSuccess {
                    _authState.value = AuthState.Success
                    _eventFlow.emit(UiEvent.NavigateToHome(it.role))
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Driver registration failed")
                }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithGoogle(idToken)
                .onSuccess {
                    _authState.value = AuthState.Success
                    _eventFlow.emit(UiEvent.NavigateToHome(it.role))
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Google sign-in failed")
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _eventFlow.emit(UiEvent.NavigateToLogin)
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    sealed class UiEvent {
        data class NavigateToHome(val role: UserRole) : UiEvent()
        object NavigateToLogin : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}

sealed class AuthEvent {
    data class Login(val email: String, val password: String, val rememberMe: Boolean) : AuthEvent()
    data class GoogleSignIn(val idToken: String) : AuthEvent()
    data class RegisterUser(
        val email: String,
        val password: String,
        val fName: String,
        val lName: String,
        val pNumber: String
    ) : AuthEvent()
    data class RegisterProvider(
        val email: String,
        val password: String,
        val fName: String,
        val lName: String,
        val pNumber: String,
        val establishmentName: String,
        val baseRate: Double,
        val ratePerKm: Double,
        val businessLat: Double,
        val businessLng: Double
    ) : AuthEvent()
    data class RegisterDriver(
        val email: String,
        val password: String,
        val fName: String,
        val lName: String,
        val pNumber: String,
        val providerId: String,
        val vehicleId: String,
        val licenseNo: String
    ) : AuthEvent()
    object Logout : AuthEvent()
}
