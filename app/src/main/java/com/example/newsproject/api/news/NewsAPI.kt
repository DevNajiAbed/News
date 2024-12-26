package com.example.newsproject.api.news

import com.example.newsproject.model.news_response.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String = "us",
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = NewsAPIUtil.API_KEY
    ): Response<NewsResponse>

    @GET("everything")
    suspend fun searchNews(
        @Query("q") searchQuery: String,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = NewsAPIUtil.API_KEY
    ): Response<NewsResponse>
}