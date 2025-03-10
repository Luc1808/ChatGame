package com.example.chatgame.auth.signup

import android.util.Log
import android.widget.Toast
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
import com.example.chatgame.auth.login.SignInState
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SignupScreen(navController: NavController) {

    val viewModel: SignUpViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var cPass by remember { mutableStateOf("") }

    LaunchedEffect(key1 = uiState.value) {

        when (uiState.value) {
            is SignUpState.Success -> {
                navController.navigate("friendlist")
            }
            is SignUpState.Error -> {
                Toast.makeText(context, "Tag name already exists", Toast.LENGTH_SHORT).show()
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
            text = "Signup Screen",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Name")}
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email")}
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pass,
            onValueChange = { pass = it },
            label = { Text(text = "Password")},
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = cPass,
            onValueChange = { cPass = it },
            label = { Text(text = "Confirm Password")},
            visualTransformation = PasswordVisualTransformation(),
            isError = pass != cPass && pass.isNotEmpty() && cPass.isNotEmpty()
        )

        Spacer(modifier = Modifier.padding(8.dp))

        if (uiState.value == SignUpState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.signUp(name, email, pass) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && cPass.isNotEmpty() && pass == cPass
            ) {
                Text(text = "Sign up")
            }

            Button(onClick = { viewModel.signUpLucas() }) {
               Text(text = "Lucas")
            }
            Button(onClick = { viewModel.signUpJohn() }) {
                Text(text = "John")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text(text = "Already have an account?")
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
//    SignupScreen(navController = NavController())
}