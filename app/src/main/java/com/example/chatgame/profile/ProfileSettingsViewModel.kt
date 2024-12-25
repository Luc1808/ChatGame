package com.example.chatgame.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSettingsViewModel : ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    fun fetchUserTagAndImage(userId: String, onTagResult: (String) -> Unit, onImageResult: (String) -> Unit) {
        db.collection("users").whereEqualTo("userId", userId).get()
            .addOnSuccessListener {
                val userTag = it.documents[0].getString("tagName")
                userTag?.let { user -> onTagResult(user) }

                db.collection("users").whereEqualTo("tagName", userTag).get()
                    .addOnSuccessListener { imageId ->
                        val userImage = imageId.documents[0].getString("userIconId")
                        if (userImage != null) {
                            onImageResult(userImage)
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
    }
}