package com.example.chatgame.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomBar(navController: NavController, currentScreen: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD5D5D5))
            .padding(10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(
            onClick = {
                if (currentScreen != "friendList") {
                    navController.navigate("friendList")
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .width(90.dp)
                .background(if (currentScreen == "friendList") Color.Gray else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Friends"
            )
        }
        IconButton(
            onClick = {
                if (currentScreen != "profileSettings") {
                    navController.navigate("profileSettings")
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .width(90.dp)
                .background(if (currentScreen == "profileSettings") Color.Gray else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Profile settings"
            )
        }
    }
}
