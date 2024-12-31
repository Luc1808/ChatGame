package com.example.chatgame.chat

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
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
                    val chatData = ChatData(sortedParticipants, "", "", Timestamp.now())

                    db.collection("chats").document(chatId).set(chatData)
                } else {
                    onResult(existingChat.id)
                }
            }


    }

    fun fetchLastMessages(tagName: String, onResult: (Map<String, MessageData>) -> Unit) {
        val lastMessages = mutableMapOf<String, MessageData>()
        val currentUser = FirebaseAuth.getInstance().currentUser

        db.collection("users").whereEqualTo("userId", currentUser?.uid).get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    val tag = it.documents[0].getString("tagName").toString()

                    db.collection("chats").whereArrayContains("participants", tag)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            querySnapshot.documents.forEach { document ->
                                val participants = document.get("participants") as? List<String>
                                val friend = participants?.find { it != tagName } ?: return@forEach
                                val lastMessage = document.getString("lastMessage") ?: ""
                                val lastMessageSender = document.getString("lastMessageSender") ?: ""
                                val timestamp = document.getTimestamp("timestamp") ?: return@forEach
                                lastMessages[friend] = MessageData(lastMessageSender, timestamp, lastMessage)
                            }
                            onResult(lastMessages)
                        }
                }
            }





    }
    fun fetchMessages(chatId: String) {

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("messageTimestamp")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
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

        val messageData = MessageData(tagName, Timestamp.now(), messageText)
        db.collection("chats").document(chatId).collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                db.collection("chats").document(chatId)
                    .update(
                        "lastMessage", messageText,
                        "lastMessageSender", tagName,
                        "timestamp", FieldValue.serverTimestamp()
                    )
            }
    }


    fun formatTimestamp(timestamp: Date): String {
        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply { time = timestamp }

        val isSameDay = now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1

        return when {
            isSameDay -> {
                // Same day: Show time
                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                formatter.format(timestamp)
            }
            isYesterday -> "Yesterday"
            else -> {
                // Older dates: Show date
                val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                formatter.format(timestamp)
            }
        }
    }


    fun groupMessagesByDate(messages: List<MessageData>): Map<String, List<MessageData>> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return messages.groupBy { message ->
            formatter.format(message.messageTimestamp.toDate())
        }
    }

    fun formatDateHeader(date: String): String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())
        val yesterdayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis() - 24 * 60 * 60 * 1000)

        return when (date) {
            currentDate -> "Today"
            yesterdayDate -> "Yesterday"
            else -> {
                val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                outputFormatter.format(inputFormatter.parse(date)!!)
            }
        }
    }
}

data class ChatData(
    val participants: List<String?> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class MessageData (
    val senderTagName: String = "",
    val messageTimestamp: Timestamp = Timestamp.now(),
    val messageText: String = ""
)