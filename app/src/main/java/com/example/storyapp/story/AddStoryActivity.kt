package com.example.storyapp.story

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.data.response.StoryResponse
import com.example.storyapp.data.retrofit.ApiConfig
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.utils.UserPreference
import com.example.storyapp.utils.dataStore
import com.example.storyapp.utils.getImageUri
import com.example.storyapp.utils.reduceFileImage
import com.example.storyapp.utils.uriToFile
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var userPreference: UserPreference

    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentImageUri?.let { uri ->
            outState.putParcelable("current_image_uri", uri)
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        userPreference = UserPreference.getInstance(this.dataStore)

        savedInstanceState?.let {
            currentImageUri = Uri.parse(it.getString("current_image_uri"))
            showImage()
        } ?: run {
            binding.previewImageView.setImageResource(R.drawable.ic_place_holder)
        }

        binding.uploadButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.btnUpload.setOnClickListener { uploadImage() }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.edDesc.text.toString()

            showLoading(true)
            lifecycleScope.launch {
                val token = withContext(Dispatchers.IO) {
                    userPreference.getUser().firstOrNull()?.token
                }
                token?.let { it ->
                    val requestBody = description.toRequestBody("text/plain".toMediaType())
                    val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                    val multipartBody = MultipartBody.Part.createFormData(
                        "photo",
                        imageFile.name,
                        requestImageFile
                    )
                    try {
                        val response = ApiConfig.getApiService(it)
                        Log.d("Photo Activity", "Request: Sending upload request to server")
                        val successResponse = response.uploadStory(multipartBody, requestBody)
                        successResponse.message?.let {
                            showToast(it)
                            Log.d("Photo Activity", "Response: $it")
                        }
                        finish()
                    } catch (e: HttpException) {
                        val error = e.response()?.errorBody()?.string()
                        val errorResponse = Gson().fromJson(error, StoryResponse::class.java)
                        errorResponse.message?.let {
                            showToast(it)
                            Log.d("Photo Activity", "Response: $it")
                        }
                    } finally {
                        showLoading(false)
                    }
                } ?: showToast(getString(R.string.empty_image_warning))
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }
}