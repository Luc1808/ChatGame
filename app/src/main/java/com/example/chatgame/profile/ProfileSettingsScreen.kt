package com.example.chatgame.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatgame.R
import com.example.chatgame.bottomBar.BottomBar
import com.example.chatgame.ui.theme.imagesList
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileSettingsScreen(navController: NavController, viewModel: ProfileSettingsViewModel) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    var isEditing by remember { mutableStateOf(false) }
    var tagName by remember { mutableStateOf("") }
    var editTagName by remember { mutableStateOf("") }

    val image = remember { mutableStateOf(R.drawable.male) } // Default image
    val newImage = remember { mutableStateOf("") }
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.fetchUserTagAndImage(currentUser.uid,
                onTagResult = { tag ->
                tagName = tag
                editTagName = tag },
                onImageResult = { imageId ->
                image.value = imagesList[imageId] ?: R.drawable.male }
            )
        }
    }

    Scaffold(
        bottomBar = {
            BottomBar(navController, "profileSettings")
        },
        content = { innerPadding ->
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { isEditing = !isEditing }) {
                   Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(id = image.value), contentDescription = "male")
                Text(text = tagName, fontSize = 37.sp, fontWeight = FontWeight.Bold)
            }
        }
    )

    if (isEditing) {
        AlertDialog(
            text = {
                Column {
                    Text(text = "Choose your image!")
                    Row (
                        modifier = Modifier
                            .background(Color.DarkGray)
                            .padding(8.dp)
                    ) {
                        imagesList.forEach { imageUnit ->
                            Image(
                                painter = painterResource(id = imageUnit.value),
                                contentDescription = "img",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable {
                                        newImage.value = imageUnit.key
                                        image.value = imageUnit.value
                                    }
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
                    }
                    if (editTagName.isNotEmpty() && editTagName != tagName) {
                        viewModel.updateUserTagName(tagName, editTagName)
                    }
                    tagName = editTagName
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