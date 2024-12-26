package com.example.newsproject.db.news

import androidx.room.TypeConverter
import com.example.newsproject.model.news_response.Source
import com.google.gson.Gson

class NewsConverters {

    @TypeConverter
    fun fromSource(source: Source): String = source.name!!

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}