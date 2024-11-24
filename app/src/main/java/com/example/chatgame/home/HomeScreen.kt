package com.example.chatgame.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
   val currentUser = FirebaseAuth.getInstance()

   Column(
       modifier = Modifier
           .fillMaxSize(),
       horizontalAlignment = Alignment.CenterHorizontally,
       verticalArrangement = Arrangement.Center
   ) {
        Text(text = "Home Screen")

       Button(onClick = {
           currentUser.signOut()
           navController.navigate("login")
       }) {
            Text(text = "Sign out")
       }
   }

}