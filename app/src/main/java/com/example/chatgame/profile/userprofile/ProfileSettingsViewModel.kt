package com.example.chatgame.profile.userprofile

import androidx.lifecycle.ViewModel
import com.example.chatgame.auth.signup.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSettingsViewModel : ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    fun fetchUser(userId: String, onResult: (User) -> Unit) {
        db.collection("users").whereEqualTo("userId", userId).get()
            .addOnSuccessListener {
                val userTag = it.documents[0].getString("tagName")

                db.collection("users").whereEqualTo("tagName", userTag).get()
                    .addOnSuccessListener { user ->
                        if (user != null) {
                            val friendId = user.documents[0].getString("userId")
                            val friendIcon = user.documents[0].getString("userIconId")
                            val friendTagName = user.documents[0].getString("tagName")
                            val friendCoverColor = user.documents[0].getString("coverColor")

                            val friendData = User(friendId!!, friendIcon!!, friendCoverColor!!, friendTagName!!, listOf(), listOf())
                            onResult(friendData)
                        }
                    }
            }
    }

    fun fetchUserImage(friendTagName: String, onResult: (String) -> Unit) {

        db.collection("users").whereEqualTo("tagName", friendTagName).get()
            .addOnSuccessListener {
                val userImage = it.documents[0].getString("userIconId")
                if (userImage != null) {
                    onResult(userImage)
                }
            }
    }

    fun updateUserIcon(tag: String, newIcon: String) {
        db.collection("users").document(tag)
            .update("userIconId", newIcon) // Update the "userIconId" field with the new value
    }

    fun updateUserTagName(tag: String, newTagName: String) {
        val userRef = db.collection("users").document(tag)
        val newUserRef = db.collection("users").document(newTagName)

        userRef.get().addOnSuccessListener { document ->
           if (document.exists()) {
               newUserRef.set(document.data ?: emptyMap<String, Any?>())
                   .addOnSuccessListener {
                       newUserRef.update("tagName", newTagName)
                           .addOnSuccessListener {
                               userRef.delete()
                           }
               }
           }
       }

        db.collection("users").whereArrayContains("friends", tag).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val friendList = document.get("friends") as MutableList<String>
                    val updatedList = friendList.map {
                        if (it == tag) newTagName else it
                    }
                    db.collection("users").document(document.id)
                        .update("friends", updatedList)
                }
            }

        db.collection("users").whereArrayContains("friendRequests", tag).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val friendRequestList = document.get("friendRequests") as MutableList<String>
                    val updatedList = friendRequestList.map {
                        if (it == tag) newTagName else it
                    }
                    db.collection("users").document(document.id)
                        .update("friendRequests", updatedList)
                }
            }
    }
}