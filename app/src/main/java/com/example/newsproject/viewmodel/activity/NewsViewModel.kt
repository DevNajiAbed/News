package com.example.newsproject.viewmodel.activity

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsproject.api.Resource
import com.example.newsproject.api.news.NewsRepository
import com.example.newsproject.app.MyApp
import com.example.newsproject.db.news.SavedNewsRepository
import com.example.newsproject.model.news_response.Article
import com.example.newsproject.model.news_response.NewsResponse
import com.example.newsproject.model.news_response.ToResumeRequest
import com.example.newsproject.util.NetworkConnectivityObserver
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.reflect.typeOf

class NewsViewModel(app: Application) : AndroidViewModel(app) {

    val breakingNewsList = ArrayList<Article>()
    private val _breakingNewsLiveData = MutableLiveData<Resource<NewsResponse>>()
    val breakingNewsLiveData: LiveData<Resource<NewsResponse>> = _breakingNewsLiveData
    private var countryCode = "us"
    var breakingNewsPage = 1
        private set

    val searchNewsList = ArrayList<Article>()
    private val _searchNews = MutableLiveData<Resource<NewsResponse>>()
    val searchNews: LiveData<Resource<NewsResponse>> = _searchNews
    var searchNewsPage = 1

    val requestsList: ArrayList<ToResumeRequest> = ArrayList()

    val tooManyRequestsLiveData = MutableLiveData(false)

    init {
        if (breakingNewsPage == 1)
            getBreakingNews()
    }

    fun getBreakingNews() = viewModelScope.launch {
        val request = ToResumeRequest.BreakingNewsRequest(countryCode, breakingNewsPage)
        if(breakingNewsPage > 1) {
            if (!requestsList.contains(request))
                requestsList.add(request)
        }

        if (hasInternetConnection()) {
            _breakingNewsLiveData.value = Resource.Loading()
            val response = NewsRepository.getBreakingNews(countryCode, breakingNewsPage)
            handleBreakingNewsResponse(response, request)
            breakingNewsPage++
        }
    }

    private fun handleBreakingNewsResponse(
        response: Resource<NewsResponse>,
        request: ToResumeRequest
    ) {
        if (response is Resource.Success) {
            response.data?.let {
                if (it.articles.isNotEmpty()) {
                    breakingNewsList.addAll(it.articles)
                    _breakingNewsLiveData.value = response
                }
                requestsList.remove(request)
            }
            tooManyRequestsLiveData.value = false
        } else {
            _breakingNewsLiveData.value = response
            Log.e("nji", "An error occurred: ${response.message}")
            response.message?.let {
                tooManyRequestsLiveData.value = it.contains("code: 429")
            }
        }
    }

    private fun changeCountryCode(countryCode: String) {
        this.countryCode = countryCode
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        val request = ToResumeRequest.SearchNewsRequest(searchQuery, searchNewsPage)
        if (!requestsList.contains(request))
            requestsList.add(request)

        if (hasInternetConnection()) {
            _searchNews.value = Resource.Loading()
            val response = NewsRepository.searchNews(searchQuery, searchNewsPage)
            handleSearchNewsResponse(response, request)
            searchNewsPage++
        }
    }

    private fun handleSearchNewsResponse(
        response: Resource<NewsResponse>,
        request: ToResumeRequest
    ) {
        if (response is Resource.Success) {
            response.data?.let {
                if (it.articles.isNotEmpty()) {
                    searchNewsList.addAll(it.articles)
                    _searchNews.value = response
                }
                requestsList.remove(request)
            }
            tooManyRequestsLiveData.value = false
        } else {
            _searchNews.value = response
            Log.e("nji", "An error occurred: ${response.message}")
            response.message?.let {
                tooManyRequestsLiveData.value = it.contains("code: 429")
            }
        }
    }

    suspend fun insertSavedArticle(context: Context, article: Article): Boolean {
        return SavedNewsRepository.insertArticle(context, article)
    }

    fun getAllSavedNews(context: Context) = SavedNewsRepository.getAllArticles(context)

    fun deleteSavedArticle(context: Context, article: Article) = viewModelScope.launch {
        SavedNewsRepository.deleteArticle(context, article)
    }

    fun cancelAllRequests() {
        NewsRepository.cancelAllRequests()
    }

    fun resumeAllRequests() {
        if (requestsList.isNotEmpty()) {
            for (request in ArrayList(requestsList)) {
                when (request) {
                    is ToResumeRequest.BreakingNewsRequest -> {
                        changeCountryCode(request.countryCode!!)
                        breakingNewsPage = request.page
                        getBreakingNews()
                    }

                    is ToResumeRequest.SearchNewsRequest -> {
                        searchNewsPage = request.page
                        searchNews(request.searchQuery!!)
                    }
                }
            }
            requestsList.clear()
        }
    }

    fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<MyApp>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}