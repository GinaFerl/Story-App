package com.example.storyapp.story

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.storyapp.data.response.DetailStoryResponse
import com.example.storyapp.data.retrofit.ApiConfig
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.main.MainViewModel
import com.example.storyapp.utils.ViewModelFactory
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, ApiConfig().getApiService("token"))
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
        viewModel.getStoryById(storyId).observe(this) { story ->
            if (story != null) {
                updateUI(story)
            } else {
                Log.e("DetailActivity", "Story is null")
            }
        }
    }

    private fun updateUI(story: DetailStoryResponse) {
        binding.tvName.text = story.story.description
        binding.tvDesc.text = story.story.name
        Glide.with(this)
            .load(story.story.photoUrl)
            .into(binding.ivStory)
    }
}