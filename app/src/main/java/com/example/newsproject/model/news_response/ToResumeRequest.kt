package com.example.newsproject.model.news_response

sealed class ToResumeRequest(
    val countryCode: String? = null,
    val searchQuery: String? = null,
    val page: Int,
) {
    class BreakingNewsRequest(countryCode: String, page: Int) :
        ToResumeRequest(countryCode = countryCode, page = page)

    class SearchNewsRequest(searchQuery: String, page: Int) :
        ToResumeRequest(searchQuery = searchQuery, page = page)
}
