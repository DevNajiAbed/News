package com.example.newsproject.db.news

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.newsproject.model.news_response.Article

object SavedNewsRepository {

    private var db: NewsDatabase? = null

    suspend fun insertArticle(context: Context, article: Article): Boolean {
        if(db == null)
            initDb(context)
        db?.let {
            if(it.dao.countArticlesWithUrl(article.url) == 0) {
                db?.dao?.insertArticle(article)
                return true
            }
        }
        return false
    }

    suspend fun deleteArticle(context: Context, article: Article) {
        if(db == null)
            initDb(context)
        db?.dao?.deleteArticle(article)
    }

    fun getAllArticles(context: Context): LiveData<List<Article>> {
        if(db == null)
            initDb(context)
        return db?.dao?.getAllArticles()!!
    }

    private fun initDb(context: Context) {
        db = Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            "news_db"
        ).build()
    }
}