package com.example.chatgame.friends.addFriend

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatgame.friends.friendList.FriendListViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FriendRequestsScreen(nav: NavController, viewModel: FriendListViewModel) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    val friendRequests = viewModel.friendRequests.observeAsState(emptyList())

    LaunchedEffect(currentUser?.uid) {
        viewModel.startListeningForFriends()
    }

    Scaffold  (
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { nav.navigate("friendList") }) {
                    Text(text = "Go back")
                }


                LazyColumn(
                    Modifier.border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
                ) {
                    items(friendRequests.value) { friendRequest ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                text = friendRequest
                            )
                            IconButton(onClick = {
                                viewModel.acceptFriendRequest(friendRequest)
                                nav.navigate("friendList")
                            }) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Accept")
                            }
                            IconButton(onClick = {
                                viewModel.declineFriendRequest(friendRequest)
                                nav.navigate("friendList")
                            }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Decline")
                            }
                        }
                    }
                }
            }
        }
    )


}

