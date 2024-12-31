package com.example.chatgame.auth.signup

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val state = _state.asStateFlow()

    fun signUp(tagName: String, email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authResult ->
                val userId = authResult.result.user?.uid
                val db = FirebaseFirestore.getInstance()

                db.collection("users").whereEqualTo("tagName", tagName).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
//                            val user = hashMapOf(
//                                "tagName" to tagName,
//                                "friends" to listOf<String>(),
//                                "friendRequests" to listOf<String>()
//                            )
                            val user = User(userId!!,"male","gray", tagName, listOf(), listOf())
                            db.collection("users").document(user.tagName).set(user)
                            _state.value = SignUpState.Success
                        } else {
                            _state.value = SignUpState.Error
                        }
                    }

            }
    }

    fun signUpLucas() {

        /* SIGN UP LUCAS */
        FirebaseAuth.getInstance().createUserWithEmailAndPassword("lucas@gmail.com", "123456")
            .addOnCompleteListener { authResult ->
                val userId = authResult.result.user?.uid
                val db = FirebaseFirestore.getInstance()

                db.collection("users").whereEqualTo("tagName", "Lucas").get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            val user = User(userId!!,"sixth","green", "Lucas", listOf(), listOf())
                            db.collection("users").document(user.tagName).set(user)
                            _state.value = SignUpState.Success
                        } else {
                            _state.value = SignUpState.Error
                            Log.e("Firestore", "User already exists")
                        }
                        Log.e("Firestore", "User already exists")
                    }

            }

    }

    fun signUpJohn() {
        /* SIGN UP TEST */
        FirebaseAuth.getInstance().createUserWithEmailAndPassword("john@gmail.com", "123456")
            .addOnCompleteListener { authResult ->
                val userId = authResult.result.user?.uid
                val db = FirebaseFirestore.getInstance()

                db.collection("users").whereEqualTo("tagName", "John").get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            val user = User(userId!!,"third","blue", "John", listOf(), listOf())
                            db.collection("users").document(user.tagName).set(user)
                            _state.value = SignUpState.Success
                        } else {
                            _state.value = SignUpState.Error
                        }
                    }

            }
    }
}

sealed class SignUpState {
    object Nothing : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    object Error : SignUpState()
}

data class User (
    val userId: String = "",
    val userIconId: String = "",
    val coverColor: String = "",
    var tagName: String = "",
    val friends: List<String> = emptyList(),
    val friendRequests: List<String> = emptyList()
)

