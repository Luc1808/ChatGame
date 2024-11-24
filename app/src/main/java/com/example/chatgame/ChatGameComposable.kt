package com.example.chatgame

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatgame.auth.login.LoginScreen
import com.example.chatgame.auth.signup.SignupScreen
import com.example.chatgame.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatGameComposable() {

    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val start = if (currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = start) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("home") { HomeScreen(navController) }
    }
}