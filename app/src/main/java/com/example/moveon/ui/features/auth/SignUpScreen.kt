package com.example.moveon.ui.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.UserRole
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightBorderLight
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.util.Constants
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    flowViewModel: AuthFlowViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRoleChoose: () -> Unit,
    onNavigateToHome: (UserRole) -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState
    val isLoading = authState is AuthViewModel.AuthState.Loading
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isCheckingEmail by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AuthViewModel.UiEvent.NavigateToHome -> onNavigateToHome(event.role)
                is AuthViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Primary, Primary.copy(alpha = 0.85f))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MoveOn",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Get Started",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Create your account to start moving",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(LightBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightBorderLight)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable(onClick = onNavigateToLogin),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign In", color = LightTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LightSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign Up", fontWeight = FontWeight.SemiBold)
                    }
                }

                SignUpField(
                    label = "Full Name",
                    value = flowViewModel.fullName,
                    onValueChange = { flowViewModel.fullName = it },
                    placeholder = "Enter your full name",
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }
                )

                SignUpField(
                    label = "Email Address",
                    value = flowViewModel.email,
                    onValueChange = { flowViewModel.email = it },
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }
                )

                SignUpField(
                    label = "Phone Number",
                    value = flowViewModel.phoneNumber,
                    onValueChange = { flowViewModel.phoneNumber = it },
                    placeholder = "0300 1234 567",
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) }
                )

                SignUpField(
                    label = "Password",
                    value = flowViewModel.password,
                    onValueChange = { flowViewModel.password = it },
                    placeholder = "Enter your password",
                    keyboardType = KeyboardType.Password,
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )

                SignUpField(
                    label = "Confirm Password",
                    value = flowViewModel.confirmPassword,
                    onValueChange = { flowViewModel.confirmPassword = it },
                    placeholder = "Re-Enter your password",
                    keyboardType = KeyboardType.Password,
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        when {
                            flowViewModel.fullName.isBlank() || flowViewModel.email.isBlank() || flowViewModel.phoneNumber.isBlank() || flowViewModel.password.isBlank() || flowViewModel.confirmPassword.isBlank() -> {
                                scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                            }
                            flowViewModel.password != flowViewModel.confirmPassword -> {
                                scope.launch { snackbarHostState.showSnackbar("Passwords do not match") }
                            }
                            flowViewModel.password.length < 6 -> {
                                scope.launch { snackbarHostState.showSnackbar("Password must be at least 6 characters") }
                            }
                            else -> {
                                scope.launch {
                                    isCheckingEmail = true
                                    val reservation = viewModel.reserveAccount(
                                        email = flowViewModel.email.trim(),
                                        pass = flowViewModel.password
                                    )
                                    isCheckingEmail = false

                                    reservation
                                        .onSuccess {
                                            onNavigateToRoleChoose()
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(
                                                error.message ?: "Could not create account right now. Please try again."
                                            )
                                        }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && !isCheckingEmail,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = if (isCheckingEmail) "Checking..." else "Create Account",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = LightBorder)
                    Text("Or continue with", color = LightTextSecondary)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = LightBorder)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(36.dp)
                        .border(width = 1.dp, color = LightBorder, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .clickable(enabled = !isLoading) {
                            scope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
                                        .build()
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()
                                    val result = credentialManager.getCredential(context, request)
                                    val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                    viewModel.onEvent(AuthEvent.GoogleSignIn(googleCredential.idToken))
                                } catch (_: GetCredentialCancellationException) {
                                    // User closed the Google credential sheet.
                                } catch (e: GetCredentialException) {
                                    snackbarHostState.showSnackbar("Google sign-in failed: ${e.message}")
                                }
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "G", fontWeight = FontWeight.SemiBold, color = Color(0xFF202124))
                }

                Text(
                    text = "By creating an account, you agree to our Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = LightTextSecondary,
                    lineHeight = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}


@Composable
private fun SignUpField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightSurfaceVariant,
                unfocusedContainerColor = LightSurfaceVariant,
                disabledContainerColor = LightSurfaceVariant,
                focusedIndicatorColor = LightBorder,
                unfocusedIndicatorColor = LightBorder
            )
        )
    }
}
