package dev.stefano.enuventory.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.components.EnuButton
import dev.stefano.enuventory.ui.components.EnuButtonVariant
import dev.stefano.enuventory.ui.components.EnuTextField
import dev.stefano.enuventory.ui.theme.EnuTheme

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val signInState by viewModel.signInState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Navigasi jika login sukses
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = EnuTheme.colors.surfaceDefaultBase
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EnuTheme.colors.surfaceDefaultBase)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header / Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Enuma Inventory Logo",
                    modifier = Modifier.size(100.dp)
                )

                Text(
                    text = "Enuventory",
                    style = EnuTheme.typography.content.headings.h3,
                    color = EnuTheme.colors.contentDefaultPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Smart inventory management",
                    style = EnuTheme.typography.ui.labels.normalCase.base,
                    color = EnuTheme.colors.contentDefaultSubtle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Email
                EnuTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Masukkan email anda",
                    label = "Email",
                    isRequired = true,
                    leadingIcon = R.drawable.ic_email
                )

                // Input Password
                EnuTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Masukkan password anda",
                    label = "Password",
                    isRequired = true,
                    leadingIcon = R.drawable.ic_lock,
                    trailingIcon = if (passwordVisible) R.drawable.ic_opened_eye else R.drawable.ic_closed_eye,
                    onTrailingIconClick = { passwordVisible = !passwordVisible },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // Error Message jika ada
                if (signInState is SignInState.Error) {
                    Text(
                        text = (signInState as SignInState.Error).message,
                        color = EnuTheme.colors.contentSignalErrorDefault,
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Login
                EnuButton(
                    text = "Masuk",
                    variant = if (signInState is SignInState.Loading) EnuButtonVariant.Loading else EnuButtonVariant.Normal,
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.signIn(email.trim(), password.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
