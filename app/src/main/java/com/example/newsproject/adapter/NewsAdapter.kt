package com.example.newsproject.adapter

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsproject.databinding.ItemArticleBinding
import com.example.newsproject.model.news_response.Article

class NewsAdapter(
    private val navController: NavController? = null,
    private val navAction: Int? = null
) : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder =
        ArticleViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.binding.apply {
            val article = differ.currentList[position]
            Glide.with(this.root).load(article.urlToImage).into(imgArticle)
            tvSourceName.text = article.source.name
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt

            root.setOnClickListener {
                navController?.navigate(
                    navAction!!,
                    Bundle().apply { putParcelable("article", article) }
                )
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size
}