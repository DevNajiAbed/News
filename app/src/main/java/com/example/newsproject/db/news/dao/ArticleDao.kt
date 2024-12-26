package com.example.newsproject.db.news.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.newsproject.model.news_response.Article

@Dao
interface ArticleDao {

    @Insert
    suspend fun insertArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<Article>>

    @Query("SELECT COUNT(*) FROM articles WHERE url = :url")
    suspend fun countArticlesWithUrl(url: String?): Int
}