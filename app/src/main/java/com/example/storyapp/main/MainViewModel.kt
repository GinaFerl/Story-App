package com.example.storyapp.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.response.DetailStoryResponse
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.User
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
): ViewModel() {
    private val _storyList = MutableLiveData<List<ListStoryItem>>()

    fun getUser(): LiveData<User> {
        return userRepository.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    suspend fun getStoryList() {
        try {
            val response = storyRepository.getStories()
            _storyList.value = response.listStory
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


     fun getStoryById(id: String): LiveData<DetailStoryResponse> {
        val detailStoryResponse = MutableLiveData<DetailStoryResponse>()
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoryById(id)
                detailStoryResponse.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return detailStoryResponse
    }

    val getStoryPager: LiveData<PagingData<ListStoryItem>> =
        storyRepository.getStoryPager().cachedIn(viewModelScope)

}