package com.example.storyapp.data.retrofit

import com.example.storyapp.data.DetailStoryResponse
import com.example.storyapp.data.LoginResponse
import com.example.storyapp.data.response.RegisterResponse
import com.example.storyapp.data.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    @Headers("Authorization: Bearer")
    suspend fun getStories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
    ): StoriesResponse

    @GET("stories/{id}")
    @Headers("Authorization: Bearer")
    suspend fun getDetailStory(
        @Path("id") id: String
    ): DetailStoryResponse

    @Multipart
    @POST("stories")
    @Headers("Authorization: Bearer")
    suspend fun uploadStory(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Float,
        @Part("lon") lon: Float
    ): RegisterResponse

}