package com.example.chatgame.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel = viewModel(), chatId: String) {

    viewModel.fetchMessages(chatId)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages = viewModel.messages.observeAsState()
    var chatInput by remember { mutableStateOf("") }


    var tagName by remember { mutableStateOf("") }
    var friendName by remember { mutableStateOf("") }

//    FirebaseFirestore.getInstance().collection("users").whereEqualTo("userId", currentUser?.uid).get()
//        .addOnSuccessListener {
//            if (it.documents.isNotEmpty()) {
//                tagName = it.documents[0].getString("tagName").toString()
//            }
//        }

    if (currentUser != null) {
        viewModel.fetchUserAndFriendNames(
            chatId = chatId,
            currentUserId = currentUser.uid
        ) { userTag, friendTag ->
            tagName = userTag
            friendName = friendTag
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
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.Gray),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.navigate("friendList") }) {
                   Icon(imageVector = Icons.Default.KeyboardArrowLeft , contentDescription = "Back")
                }
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
                    text = friendName
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.DarkGray),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    items(messages.value.orEmpty()) { message ->
                        val isCurrentUser = message.senderTagName == tagName
                        MessageBubble(messageData = message, isCurrentUser)
                    }
                }
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {  }) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Settings")
                }

                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent
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
                .clip(shape = RoundedCornerShape(40.dp))
                .padding(4.dp)
                .background(if (isCurrentUser) Color.Cyan else Color.Magenta)
                .padding(8.dp),
        ) {
            Text(text = messageData.messageText, fontSize = 17.sp)
        }
    }
}

@Preview
@Composable
fun MessageBubblePreview() {
//    MessageBubble("Text")
}
