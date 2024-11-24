package com.example.storyapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.retrofit.ApiService

class StoryPagingSource(private val apiService: ApiService): PagingSource<Int, ListStoryItem>() {
    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val next = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getStories(page = next, size = params.loadSize)
            val stories = response.listStory
            LoadResult.Page(
                data = stories,
                prevKey = if (next == INITIAL_PAGE_INDEX) null else next - 1,
                nextKey = if (stories.isEmpty()) null else next + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}
