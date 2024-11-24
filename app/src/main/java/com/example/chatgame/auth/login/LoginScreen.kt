package com.example.chatgame.auth.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController


@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    LaunchedEffect(key1 = uiState.value) {

        when (uiState.value) {
            is SignInState.Success -> {
                navController.navigate("home")
            }
            is SignInState.Error -> {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login Screen",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email")},
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pass,
            onValueChange = { pass = it },
            label = { Text(text = "Password")},
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.padding(8.dp))
        if (uiState.value == SignInState.Loading) {
           CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.signIn(email.trim(), pass.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && pass.isNotBlank() && (uiState.value == SignInState.Nothing || uiState.value == SignInState.Error)
            ) {
                Text(text = "Login")
            }
            Spacer(modifier = Modifier.padding(8.dp))

            TextButton(onClick = { navController.navigate("signup") }) {
                Text(text = "Do not have an account yet?",)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
//    LoginScreen()
}