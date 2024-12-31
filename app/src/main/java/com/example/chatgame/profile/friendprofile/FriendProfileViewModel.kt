package com.example.chatgame.profile.friendprofile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chatgame.auth.signup.User
import com.google.firebase.firestore.FirebaseFirestore

class FriendProfileViewModel : ViewModel() {

   private val db = FirebaseFirestore.getInstance()

    fun fetchFriendData(friend: String, onResult: (User) -> Unit) {
        db.collection("users").whereEqualTo("tagName", friend).get()
            .addOnSuccessListener { friendDocument ->
                if (friendDocument != null) {
                    val friendId = friendDocument.documents[0].getString("userId")
                    val friendIcon = friendDocument.documents[0].getString("userIconId")
                    val friendTagName = friendDocument.documents[0].getString("tagName")
                    val friendCoverColor = friendDocument.documents[0].getString("coverColor")

                    val friendData = User(friendId!!, friendIcon!!, friendCoverColor!!, friendTagName!!, listOf(), listOf())
                    onResult(friendData)
                }
            }
    }
}
