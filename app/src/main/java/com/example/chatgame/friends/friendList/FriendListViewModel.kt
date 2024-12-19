package com.example.chatgame.friends.friendList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)
            listenerRegistration?.remove()

            listenerRegistration = userDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log any errors
                    Log.e("Firestore", "Error fetching friends", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _friends.value = snapshot.get("friends") as List<String>? ?: emptyList()
                    _friendRequests.value = snapshot.get("friendRequests") as List<String>? ?: emptyList()
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

    // Stop listening to avoid memory leaks
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun sendFriendRequest(friendName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        db.collection("users").document(userId!!).get()
            .addOnSuccessListener {
                val currentUserTagName = it.getString("tagName")


                db.collection("users").whereEqualTo("tagName", friendName).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val targetUserRef = querySnapshot.documents[0].reference
                            if (currentUserTagName.isNullOrEmpty()) {
                                Log.e("Firestore", "Current user's tagName is missing")
                                return@addOnSuccessListener
                            }

                            targetUserRef.update("friendRequests", FieldValue.arrayUnion(currentUserTagName))
                        }
                    }
            }
    }

    fun acceptFriendRequest(friendName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val user = db.collection("users").document(userId!!)

        db.collection("users").whereEqualTo("tagName", friendName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val targetUserRef = querySnapshot.documents[0].reference

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener {
                            val currentUserTagName = it.getString("tagName")

                            user.update("friends", FieldValue.arrayUnion(friendName))
                            targetUserRef.update("friends", FieldValue.arrayUnion(currentUserTagName))
                            user.update("friendRequests", FieldValue.arrayRemove(friendName))
                        }
                }
            }
    }

    fun declineFriendRequest(friendName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val user = db.collection("users").document(userId!!)

        db.collection("users").document(userId).get()
            .addOnSuccessListener {
               user.update("friendRequests", FieldValue.arrayRemove(friendName))
            }

    }
}
