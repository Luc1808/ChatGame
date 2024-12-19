package com.example.chatgame.auth.signup

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
                            val user = hashMapOf(
                                "tagName" to tagName,
                                "friends" to listOf<String>(),
                                "friendRequests" to listOf<String>()
                            )
                            db.collection("users").document(userId!!).set(user)
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
                            val user = hashMapOf(
                                "tagName" to "Lucas",
                                "friends" to listOf<String>(),
                                "friendRequests" to listOf<String>()
                            )
                            db.collection("users").document(userId!!).set(user)
                            _state.value = SignUpState.Success
                        } else {
                            _state.value = SignUpState.Error
                        }
                    }

            }

    }

    fun signUpTest() {
        /* SIGN UP TEST */
        FirebaseAuth.getInstance().createUserWithEmailAndPassword("test@gmail.com", "123456")
            .addOnCompleteListener { authResult ->
                val userId = authResult.result.user?.uid
                val db = FirebaseFirestore.getInstance()

                db.collection("users").whereEqualTo("tagName", "Test").get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            val user = hashMapOf(
                                "tagName" to "Test",
                                "friends" to listOf<String>(),
                                "friendRequests" to listOf<String>()
                            )
                            db.collection("users").document(userId!!).set(user)
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

