package com.example.storyapp.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.data.User
import com.example.storyapp.data.repository.UserRepository

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            Log.d("LoginViewModel", "Logging in with email: $email, password: $password")
            val response = userRepository.login(email, password)
            Log.d("LoginViewModel", "Login response: $response")
            if (response.loginResult != null) {
                response.loginResult.token?.let { token ->
                    val user = User(email, token, true)
                    userRepository.saveUser(user)
                    Log.d("LoginViewModel", "User saved successfully")
                }
            }
            response
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error during login", e)
            throw e
        }
    }

    suspend fun saveUser(user: User) {
        try {
            userRepository.saveUser(user)
            Log.d("LoginViewModel", "User saved successfully")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error saving user", e)
            throw e
        }
    }
}