package com.example.chatgame.profile.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chatgame.R
import com.example.chatgame.auth.login.LoginViewModel
import com.example.chatgame.ui.theme.coverColors
import com.example.chatgame.ui.theme.imagesList
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ProfileSettingsScreen(navController: NavController, viewModel: ProfileSettingsViewModel) {


    val loginViewModel = hiltViewModel<LoginViewModel>()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var isEditing by remember { mutableStateOf(false) }
    var tagName by remember { mutableStateOf("") }
    var editTagName by remember { mutableStateOf("") }

    val coverColor = remember { mutableStateOf(Color.Blue) }
    val tmpCoverColor = remember { mutableStateOf(coverColor.value) }
    val image = remember { mutableStateOf(1) } // Default image
    val newImage = remember { mutableStateOf("") }
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.fetchUser(currentUser.uid) { user ->
                tagName = user.tagName
                coverColor.value = coverColors[user.coverColor]!!
                image.value = imagesList[user.userIconId] ?: 1
            }
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
                        IconButton(onClick = { navController.navigate("friendList") }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "friendList")
                        }
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    if (image.value != 1) {
                        Image(
                            painter = painterResource(id = image.value),
                            contentDescription = "male"
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
                    Text(text = tagName, fontSize = 37.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))

                Row {
                    Button(onClick = {
                        loginViewModel.logOut()
                        navController.navigate("login")
                    }) {
                        Text(text = "Sing out")
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Settings")
                    }
                }
            }

        }
    )

    var tmpImage = remember { mutableStateOf(1) }

    if (isEditing) {
        AlertDialog(
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Choose your image!")
                    LazyVerticalGrid (
                        columns = GridCells.Adaptive(minSize = 70.dp),
                        modifier = Modifier
                            .background(Color.DarkGray)
//                            .padding(8.dp),
                    ) {
                        items(imagesList.toList()) { imageUnit ->
                            Image(
                                painter = painterResource(id = imageUnit.second),
                                contentDescription = "img",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clickable {
                                        newImage.value = imageUnit.first
                                        tmpImage.value = imageUnit.second
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(8.dp))


                    Text(text = "Change your cover color!")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp) // Define the square size
                                    .clip(RoundedCornerShape(4.dp)) // Optional rounded corners
                                    .background(color)
                                    .border(
                                        width = if (color == tmpCoverColor.value) 4.dp else 2.dp,
                                        color = if (color == tmpCoverColor.value) Color.Black else Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { tmpCoverColor.value = color }
                            )
                        }
                    }

                    Text(text = "Change your tag name!")
                    OutlinedTextField(value = editTagName, onValueChange = { editTagName = it })
                }
            },
            onDismissRequest = { isEditing = false },
            confirmButton = {
                Button(onClick = {
                    if (newImage.value.isNotEmpty()) {
                        viewModel.updateUserIcon(tagName, newImage.value)
                        image.value = tmpImage.value
                    }
                    if (editTagName.isNotEmpty() && editTagName != tagName) {
                        viewModel.updateUserTagName(tagName, editTagName)
                        tagName = editTagName
                    }
                    coverColor.value = tmpCoverColor.value
                    isEditing = false
                }) {
                   Text(text = "Save")
                }
            },
            dismissButton = {
                Button(onClick = { isEditing = false }) {
                   Text(text = "Cancel")
                }
            }
        )
    }
}

val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Magenta,
)