package com.example.storyapp.story

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.storyapp.data.DetailStoryResponse
import com.example.storyapp.data.retrofit.ApiConfig
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.main.MainViewModel
import com.example.storyapp.utils.ViewModelFactory
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService("token"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra("storyId")
        if (storyId != null) {
            getStoryDetail(storyId)
        }
    }

    private fun getStoryDetail(storyId: String) {
        lifecycleScope.launch {
            try {
                val response = viewModel.getStoryById(storyId)
                updateUI(response)
            } catch (e: Exception) {
                Log.e("DetailActivity", "Error: ${e.message}", e)
            }
        }
    }

    private fun updateUI(response: DetailStoryResponse) {
        val story = response.story
        binding.apply {
            tvName.text = story.name
            tvDesc.text = story.description
            Glide.with(this@DetailActivity)
                .load(story.photoUrl)
                .into(ivStory)
        }
    }
}