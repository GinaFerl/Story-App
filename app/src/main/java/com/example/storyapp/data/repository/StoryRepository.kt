package com.example.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.data.response.DetailStoryResponse
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.StoriesResponse
import com.example.storyapp.data.retrofit.ApiService
import com.example.storyapp.utils.UserPreference
import kotlinx.coroutines.flow.firstOrNull

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
){
    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference)
                    .also { instance = it }
            }
    }

    suspend fun getStories(): StoriesResponse {
        try {
            userPreference.getUser().firstOrNull()?.token
                ?: throw IllegalStateException("Token not found")
            return apiService.getStories("token")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getStoryById(id: String): DetailStoryResponse {
        try {
            val token = userPreference.getUser().firstOrNull()?.token
                ?: throw IllegalStateException("Token not found")
            return apiService.getDetailStory(id, "Bearer $token") // Use the correct token format
        } catch (e: Exception) {
            throw e
        }
    }

    fun getStoryPager(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, userPreference)
            }
        ).liveData
    }

}