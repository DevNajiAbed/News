package com.example.newsproject.db.news

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsproject.db.news.dao.ArticleDao
import com.example.newsproject.model.news_response.Article

@Database(
    entities = [Article::class],
    version = 2/*,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]*/,
    exportSchema = false
)
@TypeConverters(NewsConverters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract val dao: ArticleDao
}