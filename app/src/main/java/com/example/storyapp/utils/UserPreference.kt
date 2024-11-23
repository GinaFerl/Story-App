package com.example.storyapp.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyapp.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>){
    suspend fun saveUser(user: User?) {
        user?.let {
            dataStore.edit { preferences ->
                preferences[EMAIL_KEY] = it.email
                preferences[TOKEN_KEY] = it.token
                preferences[is_login_key] = true
            }
            Log.d("UserPreference", "User saved: $user")
        } ?: run {
            Log.d("UserPreference", "User is null, not saving.")
        }
    }

    fun getUser(): Flow<User> {
        return dataStore.data.map { preferences ->
            val user = User(
                preferences[EMAIL_KEY] ?: "",
                preferences[TOKEN_KEY] ?: "",
                preferences[is_login_key] ?: false
            )
            Log.d("UserPreference", "User retrieved: $user")
            user
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        Log.d("UserPreference", "User logged out.")
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val is_login_key = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}