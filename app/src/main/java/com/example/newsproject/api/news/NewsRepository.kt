package com.example.newsproject.api.news

import com.example.newsproject.api.Resource
import com.example.newsproject.model.news_response.Article
import com.example.newsproject.model.news_response.NewsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsRepository {

    private val client: OkHttpClient by lazy {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    private val api: NewsAPI by lazy {
        Retrofit.Builder()
            .baseUrl(NewsAPIUtil.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPI::class.java)
    }

    suspend fun getBreakingNews(
        countryCode: String = "us",
        page: Int
    ): Resource<NewsResponse> {
        val response = api.getBreakingNews(countryCode, page)
        if (response.isSuccessful && response.body() != null)
            return Resource.Success(response.body()!!)
        return Resource.Error("code: ${response.code()}")
    }

    suspend fun searchNews(
        q: String,
        page: Int
    ): Resource<NewsResponse> {
        val response = api.searchNews(q, page)
        if (response.isSuccessful && response.body() != null)
            return Resource.Success(response.body()!!)
        return Resource.Error(response.message())
    }

    fun cancelAllRequests() {
        client.dispatcher().cancelAll()
    }
}