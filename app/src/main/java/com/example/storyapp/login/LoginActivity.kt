package com.example.storyapp.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.main.MainActivity
import com.example.storyapp.data.User
import com.example.storyapp.data.di.Injection
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.retrofit.ApiConfig
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.register.RegisterActivity
import com.example.storyapp.utils.UserPreference
import com.example.storyapp.utils.ViewModelFactory
import com.example.storyapp.utils.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository
    private lateinit var userPreference: UserPreference

    private val viewModel by viewModels<LoginViewModel>() {
        ViewModelFactory.getInstance(this, ApiConfig().getApiService("token"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = Injection.provideUserRepository(this, ApiConfig().getApiService("token"))
        userPreference = UserPreference.getInstance(this.dataStore)

        playAnimation()
        setupAction()
        checkUserSession()
    }

    private fun checkUserSession() {
        Log.d("LoginActivity", "Checking user session")
        lifecycleScope.launch {
            val user = userPreference.getUser().first()
            if (user.token.isNotEmpty()) {
                if (user.isLogin) {
                    Log.d("LoginActivity", "User session found. Navigating to main activity")
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("LoginActivity", "User session not found")
                }
            }
        }
    }

    private fun showAlertDialog(title: String, message: String, onClick: (() -> Unit)? = null) {
        AlertDialog.Builder(this@LoginActivity).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                onClick?.invoke()
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked")
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.d("LoginActivity", "Email: $email, Password: $password")

                lifecycleScope.launch {
                    try {
                        val response = viewModel.login(email, password)
                        when {
                            response.loginResult != null -> {
                                val user = response.loginResult.token?.let {
                                    User(email, it, true)
                                }
                                if (user != null) {
                                    viewModel.saveUser(user)
                                    withContext(Dispatchers.IO) {
                                        checkUserSession()
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                            response.message == "User not found" -> {
                                showAlertDialog(
                                    "Daftar Dulu",
                                    "Aku dengan email $email belum memiliki akun. Apakah kamu ingin mendaftar?"
                                ) {
                                    startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                                }
                            }
                            response.message == "Duplicate Email" -> {
                                showAlertDialog(
                                    "Email Sudah terdaftar",
                                    "Email $email sudah terdaftar. Apakah kamu ingin masuk?"
                                )
                            }
                            else -> {
                                showAlertDialog(
                                    "Login Gagal",
                                    "Terjadi kesalahan saat login. Silakan coba lagi."
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Error during login", e)
                        showAlertDialog(
                            "Login Gagal",
                            "Terjadi kesalahan saat login. Silakan coba lagi."
                        )
                    }
                }
            } else {
                showAlertDialog(
                    "Login Gagal",
                    "Lengkapi email dan password terlebih dahulu."
                )
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                login
            )
            startDelay = 100
        }.start()
    }
}