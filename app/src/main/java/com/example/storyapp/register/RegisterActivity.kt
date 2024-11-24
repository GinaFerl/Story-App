package com.example.storyapp.register

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
import com.example.storyapp.data.di.Injection
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.retrofit.ApiConfig
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.login.LoginActivity
import com.example.storyapp.utils.ViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRepository: UserRepository

    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService("token"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = Injection.provideUserRepository(this, ApiConfig.getApiService("token"))

        binding.loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        setupAction()
        playAnimation()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            Log.d("RegisterActivity", "Registering user with name: $name, email: $email, password: $password")

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                Log.d("RegisterActivity", "Registering user with name: $name, email: $email, password: $password")

                binding.progressBar.visibility = View.VISIBLE
                binding.signupButton.visibility = View.INVISIBLE

                lifecycleScope.launch {
                    try {
                        val response = viewModel.register(name, email, password)
                        Log.d("RegisterActivity", "Registration response: $response")

                        val dialogTitle =
                            if (!response.error!!) "Welcome" else if (response.message == "User created") "Success" else "Register Failed"
                        val dialogMessage =
                            if (!response.error) "Register Success" else if (response.message == "User created") "Register Success" else response.message
                                ?: "Please try again"

                        val isError = response.error
                        showDialog(dialogTitle, dialogMessage, isError)
                    }
                    catch (e: Exception) {
                        Log.e("RegisterActivity", "Error during registration", e)
                        showDialog("Register Failed", "Please try again", true)
                    } finally {
                        binding.progressBar.visibility = View.GONE
                        binding.signupButton.visibility = View.VISIBLE
                    }
                }
            } else {
                Log.w("RegisterActivity", "One or more fields are empty. Name: $name, Email: $email, Password: $password")
                showDialog("Register Failed", "Please fill in all fields", true)
            }
        }
    }

    private fun showDialog(title: String, message: String, isError: Boolean) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                if (!isError) {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            create()
            show()
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val nameTextView =
            ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val nameEditTextLayout =
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)


        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                signup
            )
            startDelay = 100
        }.start()
    }

}