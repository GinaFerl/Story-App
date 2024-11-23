package com.example.storyapp.register

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.RegisterResponse
import com.google.gson.Gson
import retrofit2.HttpException

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return try {
            Log.d("RegisterViewModel", "Registering user with name: $name, email: $email, password: $password")
            val response = userRepository.register(name, email, password)
            Log.d("RegisterViewModel", "Registration response: $response")
            response
        } catch (e: HttpException) {
            Log.e("RegisterViewModel", "Error during registration", e)
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
            Log.e("RegisterViewModel", "Error body: $errorBody")
            throw e
        }
    }
}