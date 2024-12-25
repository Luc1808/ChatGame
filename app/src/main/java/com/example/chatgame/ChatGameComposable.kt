package com.example.chatgame

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatgame.auth.login.LoginScreen
import com.example.chatgame.auth.signup.SignupScreen
import com.example.chatgame.chat.ChatScreen
import com.example.chatgame.chat.ChatViewModel
import com.example.chatgame.friends.addFriend.FriendRequestsScreen
import com.example.chatgame.friends.friendList.FriendListScreen
import com.example.chatgame.friends.friendList.FriendListViewModel
import com.example.chatgame.profile.ProfileSettingsScreen
import com.example.chatgame.profile.ProfileSettingsViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatGameComposable() {

    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val start = if (currentUser != null) "friendList" else "login"

    NavHost(navController = navController, startDestination = start) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("friendList") { FriendListScreen(navController, FriendListViewModel()) }
        composable("friendRequests") { FriendRequestsScreen(navController, FriendListViewModel()) }
        composable("chatScreen/{chatId}") { backStackEntry -> backStackEntry.arguments?.getString("chatId")
            ?.let { ChatScreen(navController, ChatViewModel(), it) } }
        composable("profileSettings") { ProfileSettingsScreen(navController, ProfileSettingsViewModel()) }
    }
}