package com.example.chatgame.friends.friendList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatgame.chat.ChatViewModel
import com.example.chatgame.chat.MessageData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FriendListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> get() = _friends

    private val _friendRequests = MutableLiveData<List<String>>()
    val friendRequests: LiveData<List<String>> get() = _friendRequests

    private var listenerRegistration: ListenerRegistration? = null

    // Start listening to real-time updates
    fun startListeningForFriends() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {

            db.collection("users").whereEqualTo("userId", currentUser.uid).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val tagName = querySnapshot.documents[0].getString("tagName")
                        if (!tagName.isNullOrEmpty()) {
                            val userDocRef = db.collection("users").document(tagName)

                            listenerRegistration?.remove()

                            listenerRegistration =
                                userDocRef.addSnapshotListener { snapshot, error ->
                                    if (error != null) {
                                        // Log any errors
                                        Log.e("Firestore", "Error fetching friends", error)
                                        return@addSnapshotListener
                                    }

                                    if (snapshot != null && snapshot.exists()) {
                                        _friends.value =
                                            snapshot.get("friends") as List<String>? ?: emptyList()
                                        _friendRequests.value =
                                            snapshot.get("friendRequests") as List<String>?
                                                ?: emptyList()
                                    } else {
                                        _friends.value = emptyList()
                                        _friendRequests.value = emptyList()
                                    }
                                }
                        } else {
                            _friends.value = emptyList()
                            _friendRequests.value = emptyList()
                        }
                    }
                }
        }
    }

    // Stop listening to avoid memory leaks
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun sendFriendRequest(friendName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        db.collection("users").whereEqualTo("tagName", friendName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val targetUserRef = querySnapshot.documents[0].reference
                    if (userId.isNullOrEmpty()) {
                        Log.e("Firestore", "Current user's tagName is missing")
                        return@addOnSuccessListener
                    }
                    db.collection("users").whereEqualTo("userId", userId).get()
                        .addOnSuccessListener { me ->
                            if (!me.isEmpty) {
                                val myTagName = me.documents[0].getString("tagName")

                                targetUserRef.update("friendRequests", FieldValue.arrayUnion(myTagName))
                            }
                        }
                }
            }
    }

    fun acceptFriendRequest(friendTagName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("users").document(friendTagName).get()
            .addOnSuccessListener { friend ->
                if (friend.exists()) {

                    db.collection("users").whereEqualTo("userId", userId).get()
                        .addOnSuccessListener { me ->
                            if (!me.isEmpty) {
                                val myRef = me.documents[0].reference
                                myRef.update("friends", FieldValue.arrayUnion(friendTagName))
                                friend.reference.update("friends", FieldValue.arrayUnion(me.documents[0].getString("tagName")))
                                myRef.update("friendRequests", FieldValue.arrayRemove(friendTagName))
                            }

                        }
                }
            }

    }

    fun declineFriendRequest(friendName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("users").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { me ->
                val myRef = me.documents[0].reference
                db.collection("users").whereEqualTo("tagName", friendName).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val targetUser = querySnapshot.documents[0].getString("tagName")

                            myRef.update("friendRequests", FieldValue.arrayRemove(targetUser))
                        }

                     }
            }

    }

    fun deleteFriend(friendTagName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("users").document(friendTagName).get()
            .addOnSuccessListener { friend ->
                if (friend.exists()) {

                    db.collection("users").whereEqualTo("userId", userId).get()
                        .addOnSuccessListener { me ->
                            if (!me.isEmpty) {
                                val myRef = me.documents[0].reference
                                myRef.update("friends", FieldValue.arrayRemove(friendTagName))
                                friend.reference.update("friends", FieldValue.arrayRemove(me.documents[0].getString("tagName")))
                            }

                        }
                }
            }

    }


}


