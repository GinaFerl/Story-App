package com.example.storyapp.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.retrofit.ApiConfig
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.story.AddStoryActivity
import com.example.storyapp.story.DetailActivity
import com.example.storyapp.utils.ViewModelFactory
import com.example.storyapp.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService("token"))
    }
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MainAdapter()

        viewModel.getUser().observe(this) { user ->
            if (!user.isLogin || user.token.isEmpty()) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                if (user.token.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        try {
                            viewModel.getStoryPager().observe(this@MainActivity) { pagingData ->
                                adapter.submitData(lifecycle, pagingData)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                    binding.progressBar.visibility = View.GONE
                } else {
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                }
            }
        }

        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = adapter

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }

        appBar()
        setupView()
    }

    private fun appBar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.menu_logout -> {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            signOut()
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    alertDialog.show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try {
                viewModel.getStoryList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.option_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.menu_logout -> {
//                val alertDialog = AlertDialog.Builder(this)
//                    .setTitle("Logout")
//                    .setMessage("Are you sure you want to logout?")
//                    .setPositiveButton("Yes") { dialog, _ ->
//                        signOut()
//                        dialog.dismiss()
//                    }
//                    .setNegativeButton("No") { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    .create()
//                alertDialog.show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun signOut() {
        lifecycleScope.launch {
            try {
                viewModel.logout()
                startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during logout: ${e.message}")
                Toast.makeText(this@MainActivity, "Failed to logout, please try again ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}