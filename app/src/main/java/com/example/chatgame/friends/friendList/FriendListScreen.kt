package com.example.chatgame.friends.friendList

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chatgame.R
import com.example.chatgame.auth.login.LoginViewModel
import com.example.chatgame.chat.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(navController: NavController, viewModel: FriendListViewModel) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val logInViewModel = hiltViewModel<LoginViewModel>()
    val friends = viewModel.friends.observeAsState(emptyList())
    val chatViewModel = hiltViewModel<ChatViewModel>()

    // AddFriend bottom sheet
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var tagName by remember { mutableStateOf("") }
    var friendTagName by remember { mutableStateOf("") }

    FirebaseFirestore.getInstance().collection("users").whereEqualTo("userId", currentUser?.uid).get()
        .addOnSuccessListener {
            if (it.documents.isNotEmpty()) {
                tagName = it.documents[0].getString("tagName").toString()
            }
        }

    LaunchedEffect(currentUser?.uid) {
        viewModel.startListeningForFriends()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Ensures padding for system bars
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button(onClick = { navController.navigate("friendRequests") }) {
                        Text("Friend Requests")
                    }
                    Button(onClick = {
                        if (currentUser != null) {
                            logInViewModel.logOut()
                            navController.navigate("login")
                        }
                    }) {
                        Text(text = "Logout")
                    }
                }
                Text(text = tagName)
                LazyColumn {
                    items(friends.value) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 1.dp, color = Color.Gray)
                                .padding(vertical = 4.dp)
                                .clickable {
                                        // Fetch or create the chatId for the selected friend
                                    chatViewModel.createOrGetChat(listOf(tagName, friend))
                                    { chatId -> navController.navigate("chatScreen/$chatId") }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background),
                                contentDescription = "Friend Image",
                                modifier = Modifier
                                    .size(70.dp)
                                    .aspectRatio(1f)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                text = friend
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { viewModel.deleteFriend(friend) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete friend")
                            }
                        }
                    }
                }
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = friendTagName,
                    onValueChange = { friendTagName = it },
                    label = { Text("Tag Name") }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.sendFriendRequest(friendTagName)
                                friendTagName = ""
                                showBottomSheet = false
                            }
                        }
                    }
                    ) {
                        Text("Send Request")
                    }
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}