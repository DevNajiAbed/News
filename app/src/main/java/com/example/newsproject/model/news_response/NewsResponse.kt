package com.example.newsproject.model.news_response

data class NewsResponse(
    var articles: ArrayList<Article>,
    val status: String,
    val totalResults: Int
)