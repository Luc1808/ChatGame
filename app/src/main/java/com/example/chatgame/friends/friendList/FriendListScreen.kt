package com.example.chatgame.friends.friendList

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chatgame.R
import com.example.chatgame.auth.login.LoginViewModel
import com.example.chatgame.chat.ChatViewModel
import com.example.chatgame.chat.MessageData
import com.example.chatgame.profile.userprofile.ProfileSettingsViewModel
import com.example.chatgame.ui.theme.imagesList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(navController: NavController, viewModel: FriendListViewModel) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val friends = viewModel.friends.observeAsState(emptyList())

    val profileSettingsViewModel = hiltViewModel<ProfileSettingsViewModel>()
    val chatViewModel = hiltViewModel<ChatViewModel>()
    val logInViewModel = hiltViewModel<LoginViewModel>()


    // AddFriend bottom sheet
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var tagName by remember { mutableStateOf("") }
    var friendTagName by remember { mutableStateOf("") }

    db.collection("users").whereEqualTo("userId", currentUser?.uid).get()
        .addOnSuccessListener {
            if (it.documents.isNotEmpty()) {
                tagName = it.documents[0].getString("tagName").toString()
            }
        }

    LaunchedEffect(currentUser?.uid) {
        // Loading the friendRequests
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
                        Icon(painter = painterResource(id = R.drawable.add_friend), contentDescription = "Profile Settings")
                    }
                    Button(onClick = { navController.navigate("profileSettings") }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Profile Settings")
                    }
                }
                Text(text = tagName)
                val lastMessages = remember { mutableStateOf(mapOf<String, MessageData>()) }

                LaunchedEffect(tagName) {
                    chatViewModel.fetchLastMessages(tagName) { messages ->
                        lastMessages.value = messages
                    }
                }
                LazyColumn {
                    items(friends.value) { friend ->
                        val lastMessageData = lastMessages.value[friend]
                        val lastMessage = lastMessageData?.messageText ?: ""
                        var lastMessageSender = lastMessageData?.senderTagName ?: ""
                        if (lastMessageSender == tagName) lastMessageSender = "You"
                        val lastMessageTimestamp = lastMessageData?.messageTimestamp?.toDate()?.let { date ->
                           chatViewModel.formatTimestamp(date)
                        } ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.5.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                .padding(vertical = 8.dp)
                                .clickable {
                                    // Fetch or create the chatId for the selected friend
                                    chatViewModel.createOrGetChat(
                                        listOf(
                                            tagName,
                                            friend
                                        )
                                    ) { chatId ->
                                        navController.navigate("chatScreen/$chatId")
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val friendImage = remember { mutableStateOf(1) } // Default image

                            profileSettingsViewModel.fetchUserImage(friend) { imageId ->
                                friendImage.value = imagesList[imageId] ?: R.drawable.first// Map imageId to drawable
                            }

                            if (friendImage.value != 1) {
                                Image(
                                    painter = painterResource(id = friendImage.value),
                                    contentDescription = "Friend Image",
                                    modifier = Modifier
                                        .size(70.dp)
                                        .aspectRatio(1f)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(Color.White) // Placeholder color
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    text = friend
                                )


                                Text(
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    text = if (lastMessage.isNotEmpty()) "$lastMessageSender: $lastMessage"
                                       else lastMessage)
                            }
                                

                            Text(text = lastMessageTimestamp, modifier = Modifier.padding(end = 16.dp))

//                            IconButton(onClick = { viewModel.deleteFriend(friend) }) {
//                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete friend")
//                            }
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