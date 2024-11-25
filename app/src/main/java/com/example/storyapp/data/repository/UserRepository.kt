package com.example.storyapp.data.repository

import android.util.Log
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.data.User
import com.example.storyapp.data.response.ErrorResponse
import com.example.storyapp.data.response.RegisterResponse
import com.example.storyapp.data.retrofit.ApiService
import com.example.storyapp.utils.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
){
    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
                    .also { instance = it }
            }
    }

    suspend fun saveUser(user: User) {
        userPreference.saveUser(user)
    }

    fun getUser(): Flow<User> {
        return userPreference.getUser()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            apiService.login(email, password)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            Log.e("UserRepository", "login: ${errorBody.message}")
            throw e
        }
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return try {
            val response = apiService.register(name, email, password)
            Log.d("UserRepository", "register: $response")
            response
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            Log.e("UserRepository", "register: ${errorBody.message}")
            throw e
        }

    }

}