package com.example.storyapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.storyapp.data.DetailStoryResponse
import com.example.storyapp.data.ListStoryItem
import com.example.storyapp.data.StoriesResponse
import com.example.storyapp.data.retrofit.ApiService
import com.example.storyapp.utils.UserPreference
import kotlinx.coroutines.flow.Flow
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
            return apiService.getStories()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getStoryById(id: String): DetailStoryResponse {
        try {
            userPreference.getUser().firstOrNull()?.token
                ?: throw IllegalStateException("Token not found")
            return apiService.getDetailStory(id)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getStoryPager(): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService)
            }
        ).flow
    }

}