package com.example.chatgame.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatgame.R
import com.example.chatgame.profile.userprofile.ProfileSettingsViewModel
import com.example.chatgame.ui.theme.imagesList
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel = viewModel(), chatId: String) {

    viewModel.fetchMessages(chatId)
    val profileSettingsViewModel: ProfileSettingsViewModel = viewModel()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages = viewModel.messages.observeAsState()
    var chatInput by remember { mutableStateOf("") }

    var tagName by remember { mutableStateOf("") }
    var friendName by remember { mutableStateOf("") }


    if (currentUser != null) {
        viewModel.fetchUserAndFriendNames(
            chatId = chatId,
            currentUserId = currentUser.uid
        ) { userTag, friendTag ->
            tagName = userTag
            friendName = friendTag
        }
    }


    val listState = rememberLazyListState()

    LaunchedEffect(messages.value) {
        messages.value?.let {
            if (it.isNotEmpty()) {
                listState.scrollToItem(it.size - 1)
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.navigate("friendList") }) {
                   Icon(imageVector = Icons.Default.KeyboardArrowLeft , contentDescription = "Back")
                }
                val image = remember { mutableStateOf(1) } // Default image
                // Observe changes to friendName and fetch image
                LaunchedEffect(friendName) {
                    if (friendName.isNotEmpty()) {
                        profileSettingsViewModel.fetchUserImage(friendName) { imageId ->
                            image.value = imagesList[imageId] ?: R.drawable.first
                        }
                    }
                }
                if (image.value != 1) {
                    Image(
                        painter = painterResource(id = image.value),
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

                Text(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    text = friendName,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                        navController.navigate("friendProfileScreen/$friendName")
                    }
                        .padding(vertical = 16.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    val groupedMessages = viewModel.groupMessagesByDate(messages.value.orEmpty())

                    groupedMessages.forEach { (date, messagesForDate) ->
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center // Centers the content horizontally and vertically
                            ) {
                                Text(
                                    text = viewModel.formatDateHeader(date),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                )
                            }
                        }


                        items(messagesForDate) { message ->
                            val isCurrentUser = message.senderTagName == tagName
                            MessageBubble(messageData = message, isCurrentUser)

                        }
                    }
                }
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {  }) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Settings")
                }

                TextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(48.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent,
                    )
                )

                IconButton(onClick = {
                    viewModel.addMessage(chatId, tagName, chatInput)
                    chatInput = ""
                }) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send message")
                }
            }
        }

    }

}


@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
//    ChatScreen(,"")
}

@Composable
fun MessageBubble(messageData: MessageData, isCurrentUser: Boolean) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
    ) {
        Column (
            modifier = Modifier
                .padding(vertical = 4.dp)
                .padding(
                    start = if (isCurrentUser) 48.dp else 4.dp,
                    end = if (isCurrentUser) 4.dp else 48.dp
                )
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCurrentUser) Color.Blue else Color.Gray)
                .padding(8.dp)
        ) {
            Text(
                text = messageData.messageText,
                fontSize = 17.sp,
                color = if (isCurrentUser) Color.White else Color.Black,
            )
            

            Text(text = formatMessageTime(messageData.messageTimestamp),
                fontSize = 12.sp,
                color = if (isCurrentUser) Color.LightGray else Color.DarkGray,
                modifier = Modifier.align(Alignment.End)
                )
        }
    }
}

fun formatMessageTime(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Format as `10:30 AM`
    return formatter.format(date)
}

@Preview
@Composable
fun MessageBubblePreview() {
//    MessageBubble("Text")
}
