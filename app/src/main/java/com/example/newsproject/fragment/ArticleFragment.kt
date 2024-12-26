package com.example.newsproject.fragment

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.newsproject.NewsActivity
import com.example.newsproject.databinding.FragmentArticleBinding
import com.example.newsproject.model.news_response.Article
import com.example.newsproject.viewmodel.activity.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ArticleFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentArticleBinding
    private lateinit var article: Article
    private lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticleBinding.inflate(inflater, container, false)

        grabArticleUrl()
        initViewModel()
        binding.apply {
            wvArticle.loadUrl(article.url)
            fabSave.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.insertSavedArticle(requireContext(), article))
                        Snackbar.make(it, "Saved", Snackbar.LENGTH_SHORT).show()
                    else
                        Snackbar.make(it, "Already saved", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as NewsActivity).viewModel
    }

    private fun grabArticleUrl() {
        article = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("article", Article::class.java)!!
        } else {
            requireArguments().getParcelable<Article>("article")!!
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ArticleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}