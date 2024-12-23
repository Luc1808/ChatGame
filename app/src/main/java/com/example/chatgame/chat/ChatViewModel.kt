package com.example.chatgame.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<MessageData>>()
    val messages: MutableLiveData<List<MessageData>> get() = _messages

    fun fetchUserAndFriendNames(
        chatId: String,
        currentUserId: String,
        onResult: (String, String) -> Unit
    ) {
        db.collection("users").whereEqualTo("userId", currentUserId).get()
            .addOnSuccessListener { userSnapshot ->
                if (userSnapshot.documents.isNotEmpty()) {
                    val userTagName = userSnapshot.documents[0].getString("tagName") ?: ""

                    db.collection("chats").document(chatId).get()
                        .addOnSuccessListener { chatSnapshot ->
                            val participants = chatSnapshot.get("participants") as? List<String>
                            participants?.let {
                                val friendTagName = participants.find { it != userTagName } ?: ""
                                onResult(userTagName, friendTagName)
                            }
                        }
                }
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Error fetching user and friend names", it)
            }
    }


    fun createOrGetChat(participants: List<String?>, onResult: (String) -> Unit) {
        val sortedParticipants = participants.filterNotNull().sorted()
        val me = sortedParticipants[0]
//        val otherUser = sortedParticipants[1]

        db.collection("chats").whereArrayContains("participants", me).get()
            .addOnSuccessListener { querySnapshot ->
                val existingChat = querySnapshot.documents.find { document ->
                    val participantsInChat = (document.get("participants") as List<String>).sorted()
                    participantsInChat == sortedParticipants
                }


                if (existingChat == null) {
                    val chatId = db.collection("chats").document().id
                    val chatData = ChatData(sortedParticipants, "", "", System.currentTimeMillis())

                    db.collection("chats").document(chatId).set(chatData)
                } else {
                    onResult(existingChat.id)
                }
            }


    }

    fun fetchMessages(chatId: String) {

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("messageTimestamp")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching messages", error)
                    return@addSnapshotListener
                }

                querySnapshot?.let {
                    val messageList = it.documents.mapNotNull { document ->
                        document.toObject(MessageData::class.java)
                    }
                    _messages.value = messageList
                }
            }
    }

    fun addMessage(chatId: String, tagName: String, messageText: String) {

        val messageData = MessageData(tagName, System.currentTimeMillis(), messageText)
        db.collection("chats").document(chatId).collection("messages")
            .add(messageData)
    }

}

data class ChatData(
    val participants: List<String?> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MessageData (
    val senderTagName: String = "",
    val messageTimestamp: Long = 0L,
    val messageText: String = ""
)