package com.example.newsproject.model.news_response

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    indices = [Index(value = ["url"], unique = true)]
)
data class Article(
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    @Embedded(prefix = "source_")
    val source: Source,
    val title: String,
    @ColumnInfo(name = "url")
    val url: String,
    val urlToImage: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Source::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(author)
        parcel.writeString(content)
        parcel.writeString(description)
        parcel.writeString(publishedAt)
        parcel.writeParcelable(source, flags)
        parcel.writeString(title)
        parcel.writeString(url)
        parcel.writeString(urlToImage)
        parcel.writeValue(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }

}