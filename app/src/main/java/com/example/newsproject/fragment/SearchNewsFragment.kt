package com.example.newsproject.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsproject.NewsActivity
import com.example.newsproject.R
import com.example.newsproject.adapter.NewsAdapter
import com.example.newsproject.api.Resource
import com.example.newsproject.databinding.FragmentSearchNewsBinding
import com.example.newsproject.util.NetworkConnectivityObserver
import com.example.newsproject.viewmodel.activity.NewsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchNewsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentSearchNewsBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(1000)

                val searchQuery = s.toString().trim()
                if (searchQuery.isNotBlank()) {
                    viewModel.apply {
                        searchNewsList.clear()
                        searchNewsPage = 1
                        searchNews(searchQuery)
                        binding.rvNews.scrollToPosition(0)
                    }
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }
    private var searchJob: Job? = null


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
        binding = FragmentSearchNewsBinding.inflate(inflater, container, false)

        initViewModel()
        prepareNewsRV()
        observeSearchNewsLiveData()
        onListEnds()
        observeNetworkConnectivity()
        if (!viewModel.hasInternetConnection()) {
            startNoConnectionJob()
            (requireActivity() as NewsActivity).showNoConnectionDialog()
        }
        observeTooManyRequestsLiveData()
        viewModel.tooManyRequestsLiveData.value?.let {
            if (it)
                (requireActivity() as NewsActivity).showTooManyRequestsDialog()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.etSearch.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        binding.etSearch.removeTextChangedListener(textWatcher)
    }

    private fun observeTooManyRequestsLiveData() {
        viewModel.apply {
            tooManyRequestsLiveData.observe(viewLifecycleOwner) {
                if (it)
                    (requireActivity() as NewsActivity).showTooManyRequestsDialog()
            }
        }
    }

    private var noConnectionJob: Job? = null
    private fun observeNetworkConnectivity() {
        noConnectionJob?.cancel()
        NetworkConnectivityObserver.connectivityObserver.observe(viewLifecycleOwner) { status ->
            when (status) {
                NetworkConnectivityObserver.Status.Available -> {
                    noConnectionJob?.cancel()
                    hideLoadingTitle()
                }

                NetworkConnectivityObserver.Status.Losing,
                NetworkConnectivityObserver.Status.Lost,
                NetworkConnectivityObserver.Status.Unavailable -> {
                    startNoConnectionJob()
                }
            }
        }
    }

    private fun startNoConnectionJob() {
        noConnectionJob = CoroutineScope(Dispatchers.Default).launch {
            val values =
                arrayOf("No Connection", "No Connection.", "No Connection..", "No Connection...")
            var index = 0
            while (true) {
                if (index == values.size)
                    index = 0
                withContext(Dispatchers.Main) {
                    binding.tvTitle.text = values[index]
                }
                index++
                delay(500)
            }
        }
    }

    private var isLoadingMoreSearchNews = false
    private fun onListEnds() {
        binding.rvNews.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val searchQuery = binding.etSearch.text.toString().trim()
                if (searchQuery.isNotBlank() && !recyclerView.canScrollVertically(1) && !isLoadingMoreSearchNews) {
                    isLoadingMoreSearchNews = true
                    viewModel.searchNews(searchQuery)
                    isLoadingMoreSearchNews = false
                }
            }
        })
    }

    private fun observeSearchNewsLiveData() {
        viewModel.searchNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    adapter.apply {
                        differ.submitList(viewModel.searchNewsList)
                        notifyDataSetChanged()
                    }
                    hideLoadingTitle()
                }

                is Resource.Error -> {
                    hideLoadingTitle()
                }

                is Resource.Loading -> {
                    showLoadingTitle()
                }
            }
        }
    }

    private fun hideLoadingTitle() {
        binding.tvTitle.text = "Search News"
    }

    private fun showLoadingTitle() {
        binding.tvTitle.text = "Loading More..."
    }

    private fun prepareNewsRV() {
        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this@SearchNewsFragment.adapter = NewsAdapter(
                findNavController(),
                R.id.action_navigation_search_news_to_articleFragment
            ).apply {
                differ.submitList(viewModel.searchNewsList)
            }
            adapter = this@SearchNewsFragment.adapter
        }
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as NewsActivity).viewModel
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchNewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}