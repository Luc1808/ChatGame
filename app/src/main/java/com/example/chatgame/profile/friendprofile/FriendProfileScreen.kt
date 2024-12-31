package com.example.chatgame.profile.friendprofile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatgame.R
import com.example.chatgame.auth.signup.User
import com.example.chatgame.ui.theme.coverColors
import com.example.chatgame.ui.theme.imagesList

@Composable
fun FriendProfileScreen(navController: NavController, friendName: String, viewModel: FriendProfileViewModel) {


    val image = remember { mutableStateOf(1) }
    val coverColor = remember { mutableStateOf(Color.Blue) }

    LaunchedEffect(friendName) {
        viewModel.fetchFriendData(friendName) { result ->
            image.value = imagesList[result.userIconId] ?: 1
            coverColor.value = coverColors[result.coverColor]!!
        }
    }


    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(coverColor.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "friendList")
                        }
                    }
                    if (image.value != 1) {
                        Image(
                            painter = painterResource(id = image.value),
                            contentDescription = "img"
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = friendName, fontSize = 37.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))

                Row {
                    Button(onClick = {
                    }) {
                        Text(text = "Delete")
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Settings")
                    }
                }
            }
        }
    )
}